package serposcope.controllers.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Group.Module;
import conf.IpFilter;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.Param;
import serposcope.controllers.BaseController;

@Singleton
public class ApiGroupController extends BaseController {

    @Inject
    BaseDB baseDB;

    @FilterWith(IpFilter.class)
    public Result create(Context context, @Param("name") String name) {
        try {
            if (name == null || name.isEmpty()) {
                throw new Exception("error.invalidName");
            }

            Module module = Module.values()[0];

            Group group = new Group(module, name);
            baseDB.group.insert(group);

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
}
