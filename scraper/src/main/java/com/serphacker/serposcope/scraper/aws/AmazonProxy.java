package com.serphacker.serposcope.scraper.aws;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.join;

public class AmazonProxy {

    private static final Logger LOG = LoggerFactory.getLogger(AmazonProxy.class);


    private DescribeInstancesResult getRunningDescribeInstancesResult() {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        Filter status =  new Filter("instance-state-name").withValues("running");
        Filter tagName =  new Filter("tag:Name").withValues("dev-kage-proxy*");
        DescribeInstancesRequest request = new DescribeInstancesRequest()
                .withFilters(status, tagName);
        return ec2.describeInstances(request);
    }

    public void StopAllInstance(List<String> proxies) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        Filter status = new Filter("instance-state-name").withValues("running");
        Filter ips =  new Filter("private-ip-address").withValues(proxies);
        DescribeInstancesRequest request = new DescribeInstancesRequest().withFilters(status, ips);
        DescribeInstancesResult response = ec2.describeInstances(request);

        ArrayList<String> runningInstances = new ArrayList<>();
        for (Reservation reservation : response.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                 runningInstances.add(instance.getInstanceId());
            }
        }

        for (String instanceId : runningInstances) {
            StopInstance(instanceId);
        }
    }

    public void StopInstance(String instanceId) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        StopInstancesRequest request = new StopInstancesRequest()
                .withInstanceIds(instanceId);

        LOG.info("shutdown {}", instanceId);
        ec2.stopInstances(request);
    }

    public String getInstanceId(String IpAddress) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        Filter status =  new Filter("instance-state-name").withValues("running");
        Filter ip =  new Filter("private-ip-address").withValues(IpAddress);
        DescribeInstancesRequest request = new DescribeInstancesRequest()
                .withFilters(status, ip);
        DescribeInstancesResult response = ec2.describeInstances(request);

        for (Reservation reservation : response.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                return instance.getInstanceId();
            }
        }
        return "";
    }

    public Boolean IsRunning() {
        DescribeInstancesResult response = getRunningDescribeInstancesResult();

        return response.getReservations().size() > 0;
    }
}
