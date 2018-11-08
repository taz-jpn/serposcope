package serposcope.controllers.api;

import com.google.inject.Inject;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.task.TaskManager;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.Param;
import ninja.params.Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.controllers.BaseController;
import serposcope.filters.GoogleGroupFilter;
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

    public Result renameTarget(
            @Param("groupId") String groupId,
            @Param("name") String name,
            @Param("id") Integer targetId) {

        try {
            GoogleTarget target = getTarget(targetId, Integer.parseInt(groupId));
            if (target == null) {
                throw new Exception("error.invalidWebsite");
            }

            if (name != null) {
                name = name.replaceAll("(^\\s+)|(\\s+$)", "");
            }

            if (Validator.isEmpty(name) || Validator.isEmpty(groupId)) {
                throw new Exception("error.invalidName");
            }

            if (!Pattern.matches("^[1-9]?[0-9]+$", groupId)) {
                throw new Exception("error.invalidGroupId");
            }

            target.setName(name);
            googleDB.target.rename(target);

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

    public Result delTarget(
            @Param("groupId") String groupId,
            @Params("id[]") String[] ids
    ) {
        try {
            if (taskManager.isGoogleRunning()) {
                throw new Exception("admin.google.errorTaskRunning");
            }

            if (ids == null || ids.length == 0 || groupId == null) {
                throw new Exception("error.invalidParameters");
            }

            if (!Pattern.matches("^[1-9]?[0-9]+$", groupId)) {
                throw new Exception("error.invalidGroupId");
            }

            for (String id : ids) {
                GoogleTarget target = null;
                try {
                    target = getTarget(Integer.parseInt(id), Integer.parseInt(groupId));
                } catch (Exception ex) {
                    target = null;
                }

                if (target == null) {
                    throw new Exception("error.invalidWebsite");
                }

                Group group = new Group(Integer.parseInt(groupId), Group.Module.GOOGLE, "");

                googleDB.targetSummary.deleteByTarget(target.getId());
                googleDB.rank.deleteByTarget(group.getId(), target.getId());
                googleDB.target.delete(target.getId());
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

    protected GoogleTarget getTarget(Integer targetId, Integer groupId){
        if(targetId == null){
            return null;
        }
        List<GoogleTarget> targets = googleDB.target.list(Arrays.asList(groupId));
        for (GoogleTarget target : targets) {
            if(target.getId() == targetId){
                return target;
            }
        }

        return null;
    }

    public Result addTarget(
            Context context,
            @Param("groupId") String groupId,
            @Param("type") String targetType,
            @Params("name[]") String[] names,
            @Params("pattern[]") String[] patterns
    ) {
        try {
            if (targetType == null
                    || groupId == null
                    || names == null || names.length == 0
                    || patterns == null || patterns.length == 0
                    || names.length != patterns.length) {
                throw new Exception("error.invalidParameters");
            }
            if (!Pattern.matches("^[1-9]?[0-9]+$", groupId)) {
                throw new Exception("error.invalidGroupId");
            }

            Set<GoogleTarget> targets = new HashSet<>();
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                String pattern = patterns[i];

                if (name != null) {
                    name = name.replaceAll("(^\\s+)|(\\s+$)", "");
                }

                if (pattern != null) {
                    pattern = pattern.replaceAll("(^\\s+)|(\\s+$)", "");
                }

                if (Validator.isEmpty(name)) {
                    throw new Exception("error.invalidName");
                }

                GoogleTarget.PatternType type = null;
                try {
                    type = GoogleTarget.PatternType.valueOf(targetType);
                } catch (Exception ex) {
                    throw new Exception("error.invalidTargetType");
                }

                if(GoogleTarget.PatternType.DOMAIN.equals(type) || GoogleTarget.PatternType.SUBDOMAIN.equals(type)){
                    try {
                        pattern = IDN.toASCII(pattern);
                    } catch(Exception ex) {
                        pattern = null;
                    }
                }

                if (!GoogleTarget.isValidPattern(type, pattern)) {
                    throw new Exception("error.invalidPattern");
                }

                Group group = new Group(Integer.parseInt(groupId), Group.Module.GOOGLE, "");

                targets.add(new GoogleTarget(group.getId(), name, type, pattern));
            }

            if (googleDB.target.insert(targets) < 1) {
                throw new Exception("error.internalError");
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
