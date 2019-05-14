package conf;

import ninja.*;

public class IpFilter implements Filter {

    @Override
    public Result filter(FilterChain filterChain, Context context) {

        // for DEBUG
        if (System.getProperty("serposcope", "").equals("debug")) {
            return filterChain.next(context);
        }

        String allowIps = System.getProperty("allowIps", "");
        String[] allowIpList = allowIps.split(",", 0);
        if (allowIpList.length > 0) {
            for (String ip: allowIpList) {
                if (context.getRemoteAddr().equals(ip)) {
                    return filterChain.next(context);
                }
            }
        }
        return Results.forbidden().html().template("/serposcope/views/system/403forbidden.ftl.html");
    }
}
