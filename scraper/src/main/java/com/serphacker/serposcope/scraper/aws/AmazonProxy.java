package com.serphacker.serposcope.scraper.aws;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.Thread;

public class AmazonProxy {

    final Object proxyTaskLock = new Object();

    private static final Logger LOG = LoggerFactory.getLogger(AmazonProxy.class);


    public void StartAllInstance(List<String> proxies) {
        synchronized (proxyTaskLock) {
            ArrayList<String> stoppingInstances = getSpecifyInstance(proxies, "stopped");
            if (stoppingInstances.size() > 0) {
                for (String instanceId : stoppingInstances) {
                    StartInstance(instanceId);
                    LOG.trace("sleeping 200 milliseconds");
                    try {
                        Thread.sleep(200L);
                    } catch (InterruptedException e) {
                        LOG.debug("interrupted while waiting for starting instance");
                    }
                }
            }
        }
    }

    public void StopAllInstance(List<String> proxies) {
        synchronized (proxyTaskLock) {
            ArrayList<String> runningInstances = getSpecifyInstance(proxies, "running");
            if (runningInstances.size() > 0) {
                for (String instanceId : runningInstances) {
                    StopInstance(instanceId);
                }
            }
        }
    }

    private ArrayList<String> getSpecifyInstance(List<String> proxies, String instanceStatus) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        Filter status = new Filter("instance-state-name").withValues(instanceStatus);
        Filter ips =  new Filter("private-ip-address").withValues(proxies);
        DescribeInstancesRequest request = new DescribeInstancesRequest().withFilters(status, ips);
        DescribeInstancesResult response = ec2.describeInstances(request);

        ArrayList<String> instances = new ArrayList<>();
        if (response.getReservations().size() > 0) {
            for (Reservation reservation : response.getReservations()) {
                for (Instance instance : reservation.getInstances()) {
                    instances.add(instance.getInstanceId());
                }
            }
        }
        return instances;
    }

    public void StartInstance(String instanceId) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        StartInstancesRequest request = new StartInstancesRequest()
                .withInstanceIds(instanceId);

        LOG.info("starting {}", instanceId);
        ec2.startInstances(request);
    }

    public void StopInstance(String instanceId) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        StopInstancesRequest request = new StopInstancesRequest()
                .withInstanceIds(instanceId);

        LOG.info("stopping {}", instanceId);
        ec2.stopInstances(request);
    }

    public String getInstanceId(String IpAddress) {
        synchronized (proxyTaskLock) {
            ArrayList<String> runningInstances = getSpecifyInstance(new ArrayList<>(Arrays.asList(IpAddress)), "running");

            if (runningInstances.size() > 0) {
                return runningInstances.get(0);
            }
            return "";
        }
    }

    public Boolean IsRunning(List<String> proxies) {
        synchronized (proxyTaskLock) {
            ArrayList<String> runningInstances = getSpecifyInstance(proxies, "running");

            return runningInstances.size() > 0;
        }
    }
}
