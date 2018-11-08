package serposcope.controllers.api;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.task.TaskManager;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.Param;
import ninja.params.Params;
import ninja.session.FlashScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.controllers.BaseController;
import serposcope.controllers.google.GoogleGroupController;
import serposcope.filters.AdminFilter;
import serposcope.filters.XSRFFilter;
import serposcope.helpers.Validator;

import java.net.IDN;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ApiGoogleGroupController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(ApiGoogleGroupController.class);

    @Inject
    BaseDB baseDB;

    @Inject
    GoogleDB googleDB;

    @Inject
    TaskManager taskManager;

    final Object searchLock = new Object();

    public Result rename(Context context, @Param("groupId") String groupId, @Param("name") String name) {
        try {
            if (groupId == null || name == null) {
                throw new Exception("error.invalidParameters");
            }
            if (!Pattern.matches("^[1-9]?[0-9]+$", groupId)) {
                throw new Exception("error.invalidGroupId");
            }
            if (Validator.isEmpty(name)) {
                throw new Exception("error.invalidName");
            }

            Group group = new Group(Integer.parseInt(groupId), Group.Module.GOOGLE, name);

            group.setName(name);
            if (!baseDB.group.update(group)) {
                throw new Exception("error.failedRenameGroup");
            }

            return Results
                    .ok()
                    .json()
                    .render("success", true);
        } catch (Exception ex) {
            return Results
                    .ok()
                    .json()
                    .render("success", false)
                    .render("message", ex.getMessage());
        }
    }

    public Result delete(Context context, @Param("groupId") String groupId) {
        try {
            if (groupId == null) {
                throw new Exception("error.invalidParameters");
            }
            if (!Pattern.matches("^[1-9]?[0-9]+$", groupId)) {
                throw new Exception("error.invalidGroupId");
            }

            Group group = new Group(Integer.parseInt(groupId), Group.Module.GOOGLE, "");

            if (taskManager.isGoogleRunning()) {
                throw new Exception("admin.google.errorTaskRunning");
            }

            List<GoogleTarget> targets = googleDB.target.list(Arrays.asList(group.getId()));
            for (GoogleTarget target : targets) {
                googleDB.targetSummary.deleteByTarget(target.getId());
                googleDB.rank.deleteByTarget(group.getId(), target.getId());
                googleDB.target.delete(target.getId());
            }

            List<GoogleSearch> searches = googleDB.search.listByGroup(Arrays.asList(group.getId()));
            for (GoogleSearch search : searches) {
                deleteSearch(group, search);
            }

            baseDB.event.delete(group);
            baseDB.user.delPerm(group);
            if (!baseDB.group.delete(group)) {
                throw new Exception("admin.google.failedDeleteGroup");
            } else {
                return Results
                        .ok()
                        .json()
                        .render("success", true);
            }
        } catch (Exception ex) {
            return Results
                    .ok()
                    .json()
                    .render("success", false)
                    .render("message", ex.getMessage());
        }
    }

    protected void deleteSearch(Group group, GoogleSearch search) {
        synchronized (searchLock) {
            googleDB.search.deleteFromGroup(search, group.getId());
            googleDB.rank.deleteBySearch(group.getId(), search.getId());
            if (!googleDB.search.hasGroup(search)) {
                googleDB.serp.deleteBySearch(search.getId());
                googleDB.search.delete(search);
            }
        }
    }
}
