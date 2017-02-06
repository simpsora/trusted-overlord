package com.beeva.trustedoverlord.service.impl;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.support.AWSSupport;
import com.amazonaws.services.support.AWSSupportClientBuilder;
import com.amazonaws.services.support.model.*;
import com.beeva.trustedoverlord.model.ProfileChecks;
import com.beeva.trustedoverlord.service.TrustedOverlordService;

import java.util.HashMap;
import java.util.Map;

public class TrustedOverlordServiceImpl implements TrustedOverlordService {

    private Map<String,AWSSupport> awsSupportMap;

    public TrustedOverlordServiceImpl(final String[] profiles) {
        awsSupportMap = new HashMap<>(profiles.length);
        for(String profile : profiles) {
            AWSSupport awsSupport = AWSSupportClientBuilder.standard()
                    .withCredentials(new ProfileCredentialsProvider(profile))
                    .withRegion("us-east-1").build();
            awsSupportMap.put(profile, awsSupport);
        }
    }

    public ProfileChecks getProfileChecks(final String profile) {

        ProfileChecks result = new ProfileChecks(profile);

        DescribeTrustedAdvisorChecksResult describeServicesResult = awsSupportMap.get(profile)
                .describeTrustedAdvisorChecks(new DescribeTrustedAdvisorChecksRequest().withLanguage("en"));

        for(TrustedAdvisorCheckDescription trustedAdvisorCheckDescription : describeServicesResult.getChecks()) {

            TrustedAdvisorCheckResult trustedAdvisorCheckResult = awsSupportMap
                    .get(profile).describeTrustedAdvisorCheckResult(new DescribeTrustedAdvisorCheckResultRequest()
                            .withCheckId(trustedAdvisorCheckDescription.getId()).withLanguage("en")).getResult();

            if("error".equals(trustedAdvisorCheckResult.getStatus())) {
                result.addError(trustedAdvisorCheckDescription.getName());
            }
            else if("warning".equals(trustedAdvisorCheckResult.getStatus())) {
                result.addWarning(trustedAdvisorCheckDescription.getName());
            }
        }

        return result;

    }

}
