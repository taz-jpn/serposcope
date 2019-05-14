/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package serposcope.services;

import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.base.ConfigDB;
import com.serphacker.serposcope.db.base.PruneDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Config;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Group.Module;
import com.serphacker.serposcope.models.base.Proxy;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.scraper.aws.AmazonProxy;
import com.serphacker.serposcope.task.TaskManager;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.scheduler.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CronService implements Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(CronService.class);

    LocalTime previousCheck = null;
    ScheduledExecutorService executor;

    @com.google.inject.Inject
    GoogleDB googleDB;

    @com.google.inject.Inject
    BaseDB baseDB;

    @Inject
    TaskManager manager;
    
    @Inject
    ConfigDB configDB;
    
    @Inject
    PruneDB pruneDB;
    
    @Start(order = 90)
    public void startService() {
        LOG.info("startService");
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this,0, 30, TimeUnit.SECONDS);
    }

    @Dispose(order = 90)
    public void stopService() {
       LOG.info("stopService");
       try{executor.shutdownNow();}catch(Exception ex){}
    }    
    
    @Override
    public void run() {
        LocalTime now = LocalTime.now();
        if(previousCheck != null && now.getMinute() == previousCheck.getMinute()){
            return;
        }
        
        previousCheck = now;
        
        Config config = configDB.getConfig();
        if(config.getCronTime() == null){
            return;
        }

        // get cron time
        int hour = config.getCronTime().getHour();
        int minute = config.getCronTime().getMinute();

        // make the proxy startup time(10 minutes before cron time)
        int proxyHour = hour;
        int proxyMinute = minute;
        if (proxyMinute >= 10) {
            proxyMinute -= 10;
        } else {
            proxyMinute = minute + 50;
            if (proxyHour >= 1) {
                proxyHour--;
            } else {
                proxyHour = 23;
            }
        }
//        LOG.debug("proxyHour={}, proxyMinute={}", proxyHour, proxyMinute);

        // check proxy startup time
        if (proxyHour == now.getHour() && proxyMinute == now.getMinute()) {
            LOG.debug("starting proxy server via cron");
            List<String> proxyList = baseDB.proxy.list().stream().map(Proxy::getIp).collect(Collectors.toList());
            AmazonProxy amazonProxy = new AmazonProxy();
            amazonProxy.StartAllInstance(proxyList);
        }

        if(hour != now.getHour() || minute != now.getMinute()){
            return;
        }

        if(manager.startGoogleTask(new Run(Run.Mode.CRON, Module.GOOGLE, LocalDateTime.now()))){
            LOG.debug("starting google task via cron");
        } else {
            LOG.debug("failed to start google task via cron, this task is already running");
            return;
        }
        
        try {
            manager.joinGoogleTask();
        }catch(InterruptedException ex){
            LOG.debug("interrupted while waiting for google task");
            return;
        }
        
        if(config.getPruneRuns() > 0){
            long pruned = pruneDB.prune(config.getPruneRuns());
            LOG.info("history pruning : {} runs deleted", pruned);
        } else {
            LOG.info("history pruning is disabled");
        }
        
    }


}
