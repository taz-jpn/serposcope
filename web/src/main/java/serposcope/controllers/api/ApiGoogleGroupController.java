package serposcope.controllers.api;

import com.google.inject.Inject;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.scraper.google.GoogleDevice;
import com.serphacker.serposcope.task.TaskManager;
import conf.IpFilter;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.Param;
import ninja.params.Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.controllers.BaseController;
import serposcope.helpers.Validator;

import java.net.IDN;
import java.util.*;
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

    @FilterWith(IpFilter.class)
    public Result delSearch(
            @Param("groupId") String groupId,
            @Params("id[]") String[] ids
    ) {
        try {
            if (ids == null || ids.length == 0) {
                throw new Exception("error.noSearchSelected");
            }

            if (!Pattern.matches("^[1-9]?[0-9]+$", groupId) || groupId == null) {
                throw new Exception("error.invalidGroupId");
            }

            List<GoogleSearch> searches = new ArrayList<>();
            for (String id : ids) {
                GoogleSearch search = null;
                try {
                    search = getSearch(Integer.parseInt(id), Integer.parseInt(groupId));
                } catch (Exception ex) {
                    search = null;
                }

                if (search == null) {
                    throw new Exception("error.invalidSearch");
                }

                searches.add(search);
            }

            if (taskManager.isGoogleRunning()) {
                throw new Exception("admin.google.errorTaskRunning");
            }

            Group group = new Group(Integer.parseInt(groupId), Group.Module.GOOGLE, "");

            for (GoogleSearch search : searches) {
                deleteSearch(group, search);
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

    @FilterWith(IpFilter.class)
    public Result addSearch(
            Context context,
            @Param("groupId") String groupId,
            @Params("keyword[]") String[] keywords,
            @Params("tld[]") String tlds[], @Params("datacenter[]") String[] datacenters,
            @Params("device[]") Integer[] devices,
            @Params("local[]") String[] locals, @Params("custom[]") String[] customs
    ) {
        try {
            if (groupId == null || keywords == null || tlds == null || datacenters == null || devices == null || locals == null || customs == null
                    || keywords.length != tlds.length || keywords.length != datacenters.length || keywords.length != devices.length
                    || keywords.length != locals.length || keywords.length != customs.length) {
                throw new Exception("error.invalidParameters");
            }

            if (!Pattern.matches("^[1-9]?[0-9]+$", groupId)) {
                throw new Exception("error.invalidGroupId");
            }

            Set<GoogleSearch> searches = new HashSet<>();

            for (int i = 0; i < keywords.length; i++) {
                GoogleSearch search = new GoogleSearch();

                if (keywords[i].isEmpty()) {
                    throw new Exception("admin.google.keywordEmpty");
                }
                search.setKeyword(keywords[i]);

                if (!Validator.isGoogleTLD(tlds[i])) {
                    throw new Exception("admin.google.invalidTLD");
                }
                search.setTld(tlds[i]);

                if (!datacenters[i].isEmpty()) {
                    if (!Validator.isIPv4(datacenters[i])) {
                        throw new Exception("error.invalidIP");
                    }
                    search.setDatacenter(datacenters[i]);
                }

                if (devices[i] != null && devices[i] >= 0 && devices[i] < GoogleDevice.values().length) {
                    search.setDevice(GoogleDevice.values()[devices[i]]);
                } else {
                    search.setDevice(GoogleDevice.DESKTOP);
                }

                if (!Validator.isEmpty(locals[i])) {
                    search.setLocal(locals[i]);
                }

                if (!Validator.isEmpty(customs[i])) {
                    search.setCustomParameters(customs[i]);
                }

                searches.add(search);
            }

            List<GoogleSearch> knownSearches = new ArrayList<>();
            synchronized (searchLock) {
                for (GoogleSearch search : searches) {
                    int id = googleDB.search.getId(search);
                    if (id > 0) {
                        search.setId(id);
                        knownSearches.add(search);
                    }
                }

                Group group = new Group(Integer.parseInt(groupId), Group.Module.GOOGLE, "");

                googleDB.search.insert(searches, group.getId());
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

    @FilterWith(IpFilter.class)
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

    @FilterWith(IpFilter.class)
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

    private GoogleSearch getSearch(Integer searchId, Integer groupId){
        if(searchId == null){
            return null;
        }
        List<GoogleSearch> searches = googleDB.search.listByGroup(Arrays.asList(groupId));
        for (GoogleSearch search : searches) {
            if(search.getId() == searchId){
                return search;
            }
        }

        return null;
    }

    private GoogleTarget getTarget(Integer targetId, Integer groupId){
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

    @FilterWith(IpFilter.class)
    public Result addTarget(
            Context context,
            @Param("groupId") String groupId,
            @Param("type") String targetType,
            @Param("name") String name,
            @Param("pattern") String pattern
    ) {
        try {
            if (targetType == null
                    || groupId == null
                    || name == null
                    || pattern == null) {
                throw new Exception("error.invalidParameters");
            }
            if (!Pattern.matches("^[1-9]?[0-9]+$", groupId)) {
                throw new Exception("error.invalidGroupId");
            }

            // get registered target list
            List<GoogleTarget> targetList = googleDB.target.list(null);
            for (GoogleTarget target: targetList) {
                if (target.getPattern().equals(pattern)) {
                    return Results
                            .ok()
                            .json()
                            .render("success", true)
                            .render("id", target.getId());
                }
            }

            Set<GoogleTarget> targets = new HashSet<>();

            name = name.replaceAll("(^\\s+)|(\\s+$)", "");

            pattern = pattern.replaceAll("(^\\s+)|(\\s+$)", "");

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

            int id = googleDB.target.insert(targets);
            if (id < 1) {
                throw new Exception("error.internalError");
            }
            return Results
                    .ok()
                    .json()
                    .render("success", true)
                    .render("id", id);
        } catch (Exception ex) {
            return Results
                    .ok()
                    .json()
                    .render("success", false)
                    .render("message", ex.getMessage());
        }
    }

    @FilterWith(IpFilter.class)
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

    @FilterWith(IpFilter.class)
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

    private void deleteSearch(Group group, GoogleSearch search) {
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
