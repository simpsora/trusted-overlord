package com.beeva.trustedoverlord.service.impl;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.health.AWSHealth;
import com.amazonaws.services.health.AWSHealthClientBuilder;
import com.amazonaws.services.health.model.*;
import com.amazonaws.services.support.AWSSupport;
import com.amazonaws.services.support.AWSSupportClientBuilder;
import com.amazonaws.services.support.model.*;
import com.beeva.trustedoverlord.model.ProfileChecks;
import com.beeva.trustedoverlord.model.ProfileHealth;
import com.beeva.trustedoverlord.service.TrustedOverlordService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TrustedOverlordServiceImpl implements TrustedOverlordService {

    private Map<String,AWSSupport> awsSupportMap;
    private Map<String,AWSHealth> awsHealthMap;

    public TrustedOverlordServiceImpl(final String[] profiles) {

        awsSupportMap = new HashMap<>(profiles.length);
        awsHealthMap = new HashMap<>(profiles.length);
        ProfileCredentialsProvider profileCredentialsProvider;

        for(String profile : profiles) {

            profileCredentialsProvider = new ProfileCredentialsProvider(profile);

            AWSSupport awsSupport = AWSSupportClientBuilder.standard()
                    .withCredentials(profileCredentialsProvider)
                    .withRegion(Regions.US_EAST_1.getName()).build();
            awsSupportMap.put(profile, awsSupport);

            AWSHealth awsHealth = AWSHealthClientBuilder.standard()
                    .withCredentials(profileCredentialsProvider)
                    .withRegion(Regions.US_EAST_1.getName()).build();
            awsHealthMap.put(profile, awsHealth);
        }

    }

    public ProfileChecks getProfileChecks(final String profile) {

        ProfileChecks result = new ProfileChecks();

        DescribeTrustedAdvisorChecksResult describeServicesResult = awsSupportMap.get(profile)
                .describeTrustedAdvisorChecks(new DescribeTrustedAdvisorChecksRequest()
                        .withLanguage(Locale.ENGLISH.getLanguage()));

        for(TrustedAdvisorCheckDescription trustedAdvisorCheckDescription : describeServicesResult.getChecks()) {

            TrustedAdvisorCheckResult trustedAdvisorCheckResult = awsSupportMap
                    .get(profile).describeTrustedAdvisorCheckResult(new DescribeTrustedAdvisorCheckResultRequest()
                            .withCheckId(trustedAdvisorCheckDescription.getId())
                            .withLanguage(Locale.ENGLISH.getLanguage())).getResult();
            if("error".equals(trustedAdvisorCheckResult.getStatus())) {
                result.addError(trustedAdvisorCheckDescription.getName());
            }
            else if("warning".equals(trustedAdvisorCheckResult.getStatus())) {
                result.addWarning(trustedAdvisorCheckDescription.getName());
            }
        }

        return result;

    }

    public ProfileHealth getProfileHealth(final String profile) {

        ProfileHealth result = new ProfileHealth();
        String nextToken = null;

        do {

            DescribeEventsResult describeEventsResult = awsHealthMap.get(profile)
                    .describeEvents(new DescribeEventsRequest().withFilter(new EventFilter()
                            .withEventStatusCodes(EventStatusCode.Open, EventStatusCode.Upcoming))
                    .withNextToken(nextToken));

            for (Event event : describeEventsResult.getEvents()) {
                // TODO: Blame AWS for not using the same type for both values
                if (event.getEventTypeCategory().equals(EventTypeCategory.Issue.toString())) {
                    result.addOpenIssue(event.getEventTypeCode());
                } else if (event.getEventTypeCategory().equals(EventTypeCategory.AccountNotification.toString())) {
                    result.addOtherNotifications(event.getEventTypeCode());
                } else if ((event.getEventTypeCategory().equals(EventTypeCategory.ScheduledChange.toString()))) {
                    result.addScheduledChange(event.getEventTypeCode());
                }
            }

            nextToken = describeEventsResult.getNextToken();

        } while(nextToken!=null && !nextToken.isEmpty());

        return result;

    }

}
