package com.serphacker.serposcope.scraper.aws;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmazonProxy {

    private static final Logger LOG = LoggerFactory.getLogger(AmazonProxy.class);

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
}
