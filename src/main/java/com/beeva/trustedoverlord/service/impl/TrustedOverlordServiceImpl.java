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

    @Override
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

    @Override
    public Future<ProfileHealth> getProfileHealth(final String profile) {

        ProfileHealth profileHealth = new ProfileHealth();
        CompletableFuture<ProfileHealth> future = new CompletableFuture<>();

        describeEventAsync(profile, null, profileHealth, future);

        return future;

    }

    @Override
    public void shutdown(TrustedApi trustedApi) {
        switch (trustedApi){
            case SUPPORT: awsSupportMap.forEach((key, asyncClient) -> asyncClient.shutdown()); break;
            case HEALTH: awsHealthMap.forEach((key, asyncClient) -> asyncClient.shutdown()); break;
        }


    }

    private class TrustedAdvisorChecksResultHandler implements AsyncHandler<DescribeTrustedAdvisorChecksRequest, DescribeTrustedAdvisorChecksResult> {

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

            List<Future<DescribeTrustedAdvisorCheckResultResult>> futures = new ArrayList<>();

            describeTrustedAdvisorChecksResult.getChecks()
                    .forEach(checkDescription ->
                            futures.add(
                                    awsSupportMap.get(this.profile)
                                            .describeTrustedAdvisorCheckResultAsync(
                                                    new DescribeTrustedAdvisorCheckResultRequest()
                                                            .withCheckId(checkDescription.getId())
                                                            .withLanguage(Locale.ENGLISH.getLanguage()),
                                                    new DescribeTrustedAdvisorChecksResultHandler(checkDescription, this.profileChecks)
                                            )
                            )
                    );

            waitForFuturesToComplete(futures);

            this.profileChecksFuture.complete(this.profileChecks);
        }

        private void waitForFuturesToComplete(List<Future<DescribeTrustedAdvisorCheckResultResult>> futures) {
            futures.forEach(future -> {
                try {
                    future.get(2, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
                }
            });
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
        public void onError(Exception e) {
        }

        @Override
        public void onSuccess(DescribeTrustedAdvisorCheckResultRequest request,
                              DescribeTrustedAdvisorCheckResultResult describeTrustedAdvisorCheckResult) {
            if ("error".equals(describeTrustedAdvisorCheckResult.getResult().getStatus())) {
                profileChecks.addError(checkDescription.getName());
            } else if ("warning".equals(describeTrustedAdvisorCheckResult.getResult().getStatus())) {
                profileChecks.addWarning(checkDescription.getName());
            }
        }
    }

    private void describeEventAsync(String profile, String nextToken, final ProfileHealth profileHealth, final CompletableFuture<ProfileHealth> future) {
        awsHealthMap.get(profile)
                .describeEventsAsync(new DescribeEventsRequest().withFilter(
                        new EventFilter()
                                .withEventStatusCodes(EventStatusCode.Open, EventStatusCode.Upcoming))
                                .withNextToken(nextToken),
                        new AsyncHandler<DescribeEventsRequest, DescribeEventsResult>() {
                            @Override
                            public void onError(Exception exception) {
                                future.completeExceptionally(exception);
                            }

                            @Override
                            public void onSuccess(DescribeEventsRequest request, DescribeEventsResult describeEventsResult) {
                                for (Event event : describeEventsResult.getEvents()) {
                                    // TODO: Blame AWS for not using the same type for both values
                                    if (event.getEventTypeCategory().equals(EventTypeCategory.Issue.toString())) {
                                        profileHealth.addOpenIssue(event.getEventTypeCode());
                                    } else if (event.getEventTypeCategory().equals(EventTypeCategory.AccountNotification.toString())) {
                                        profileHealth.addOtherNotifications(event.getEventTypeCode());
                                    } else if ((event.getEventTypeCategory().equals(EventTypeCategory.ScheduledChange.toString()))) {
                                        profileHealth.addScheduledChange(event.getEventTypeCode());
                                    }
                                }

                                String returnedNextToken = describeEventsResult.getNextToken();
                                if (returnedNextToken != null && !returnedNextToken.isEmpty()){
                                    describeEventAsync(profile, returnedNextToken, profileHealth, future);
                                }
                                else {
                                    future.complete(profileHealth);
                                }
                            }
                        });
    }
}
