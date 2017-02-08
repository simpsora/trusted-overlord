package com.beeva.trustedoverlord.service.impl;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.health.AWSHealthAsync;
import com.amazonaws.services.health.AWSHealthAsyncClientBuilder;
import com.amazonaws.services.health.model.DescribeEventsRequest;
import com.amazonaws.services.health.model.DescribeEventsResult;
import com.amazonaws.services.health.model.Event;
import com.amazonaws.services.health.model.EventFilter;
import com.amazonaws.services.health.model.EventStatusCode;
import com.amazonaws.services.health.model.EventTypeCategory;
import com.amazonaws.services.support.AWSSupportAsync;
import com.amazonaws.services.support.AWSSupportAsyncClientBuilder;
import com.amazonaws.services.support.model.DescribeTrustedAdvisorCheckResultRequest;
import com.amazonaws.services.support.model.DescribeTrustedAdvisorCheckResultResult;
import com.amazonaws.services.support.model.DescribeTrustedAdvisorChecksRequest;
import com.amazonaws.services.support.model.DescribeTrustedAdvisorChecksResult;
import com.amazonaws.services.support.model.TrustedAdvisorCheckDescription;
import com.beeva.trustedoverlord.model.ProfileChecks;
import com.beeva.trustedoverlord.model.ProfileHealth;
import com.beeva.trustedoverlord.service.TrustedOverlordService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TrustedOverlordServiceImpl implements TrustedOverlordService {

    private Map<String, AWSSupportAsync> awsSupportMap;
    private Map<String, AWSHealthAsync> awsHealthMap;

    public TrustedOverlordServiceImpl(final String[] profiles) {

        awsSupportMap = new HashMap<>(profiles.length);
        awsHealthMap = new HashMap<>(profiles.length);
        ProfileCredentialsProvider profileCredentialsProvider;

        for (String profile : profiles) {

            profileCredentialsProvider = new ProfileCredentialsProvider(profile);

            AWSSupportAsync awsSupport = AWSSupportAsyncClientBuilder.standard()
                    .withCredentials(profileCredentialsProvider)
                    .withRegion(Regions.US_EAST_1.getName()).build();
            awsSupportMap.put(profile, awsSupport);

            AWSHealthAsync awsHealth = AWSHealthAsyncClientBuilder.standard()
                    .withCredentials(profileCredentialsProvider)
                    .withRegion(Regions.US_EAST_1.getName()).build();
            awsHealthMap.put(profile, awsHealth);
        }

    }

    public Future<ProfileChecks> getProfileChecks(final String profile) {

        CompletableFuture<ProfileChecks> result = new CompletableFuture<>();

        ProfileChecks checks = new ProfileChecks();

        awsSupportMap.get(profile)
                .describeTrustedAdvisorChecksAsync(
                        new DescribeTrustedAdvisorChecksRequest().withLanguage(Locale.ENGLISH.getLanguage()),
                        new TrustedAdvisorChecksResultHandler(profile, checks, result)
                );

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

        } while (nextToken != null && !nextToken.isEmpty());

        return result;

    }

    @Override
    public void shutdown(){
        awsSupportMap.forEach((key, asyncClient) -> asyncClient.shutdown());
    }

    private class TrustedAdvisorChecksResultHandler implements AsyncHandler<DescribeTrustedAdvisorChecksRequest, DescribeTrustedAdvisorChecksResult>{

        private String profile;
        private ProfileChecks profileChecks;
        private CompletableFuture<ProfileChecks> profileChecksFuture;

        public TrustedAdvisorChecksResultHandler(String profile, ProfileChecks profileChecks, CompletableFuture<ProfileChecks> profileChecksFuture) {
            this.profile = profile;
            this.profileChecks = profileChecks;
            this.profileChecksFuture = profileChecksFuture;
        }

        @Override
        public void onError(Exception e) {
            profileChecksFuture.completeExceptionally(e);
        }

        @Override
        public void onSuccess(DescribeTrustedAdvisorChecksRequest request,
                              DescribeTrustedAdvisorChecksResult describeTrustedAdvisorChecksResult) {

            describeTrustedAdvisorChecksResult.getChecks()
                    .forEach(checkDescription -> {

                        List<Future<DescribeTrustedAdvisorCheckResultResult>> futures = new ArrayList<>();
                        futures.add(
                                awsSupportMap.get(this.profile)
                                    .describeTrustedAdvisorCheckResultAsync(
                                            new DescribeTrustedAdvisorCheckResultRequest()
                                                    .withCheckId(checkDescription.getId())
                                                    .withLanguage(Locale.ENGLISH.getLanguage()),
                                            new DescribeTrustedAdvisorChecksResultHandler(checkDescription, this.profileChecks)
                                    )
                        );

                        futures.forEach(future -> {
                            try {
                                future.get(2, TimeUnit.SECONDS);
                            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {}
                        });
                    });

            this.profileChecksFuture.complete(this.profileChecks);
        }
    }

    private class DescribeTrustedAdvisorChecksResultHandler implements AsyncHandler<DescribeTrustedAdvisorCheckResultRequest, DescribeTrustedAdvisorCheckResultResult> {

        TrustedAdvisorCheckDescription checkDescription;
        ProfileChecks profileChecks;


        public DescribeTrustedAdvisorChecksResultHandler(TrustedAdvisorCheckDescription checkDescription, ProfileChecks profileChecks) {
            this.checkDescription = checkDescription;
            this.profileChecks = profileChecks;
        }

        @Override
        public void onError(Exception e) {}

        @Override
        public void onSuccess(DescribeTrustedAdvisorCheckResultRequest request,
                DescribeTrustedAdvisorCheckResultResult describeTrustedAdvisorCheckResult) {
            if("error".equals(describeTrustedAdvisorCheckResult.getResult().getStatus())) {
                profileChecks.addError(checkDescription.getName());
            } else if ("warning".equals(describeTrustedAdvisorCheckResult.getResult().getStatus())) {
                profileChecks.addWarning(checkDescription.getName());
            }
        }
    }
}
