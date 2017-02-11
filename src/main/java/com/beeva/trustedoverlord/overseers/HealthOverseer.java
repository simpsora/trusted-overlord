package com.beeva.trustedoverlord.overseers;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.health.AWSHealthAsync;
import com.amazonaws.services.health.AWSHealthAsyncClientBuilder;
import com.amazonaws.services.health.model.*;
import com.beeva.trustedoverlord.model.ProfileHealth;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Created by Beeva
 */
public class HealthOverseer implements Overseer{

    private AWSHealthAsync client;
    private boolean autoshutdown = false;


    public HealthOverseer(String profile) {
        this(AWSHealthAsyncClientBuilder
                .standard()
                    .withCredentials(new ProfileCredentialsProvider(profile))
                    .withRegion(Regions.US_EAST_1.getName())
                .build());
    }

    private HealthOverseer(AWSHealthAsync client){
        this.client = client;
    }

    public Future<ProfileHealth> getProfileHealth() {

        ProfileHealth profileHealth = new ProfileHealth();
        CompletableFuture<ProfileHealth> future = new CompletableFuture<>();

        describeEventAsync(null, profileHealth, future);

        return future;

    }

    public static HealthOverseer withClient(AWSHealthAsync client) {
        if (client == null){
            return new HealthOverseer(
                        AWSHealthAsyncClientBuilder.standard()
                                .withRegion(Regions.US_EAST_1.getName())
                                .build()
                        );
        }
        else {
            return new HealthOverseer(client);
        }
    }

    @Override
    public void shutdown() {
        this.client.shutdown();
    }

    @Override
    @SuppressWarnings("unchecked")
    public HealthOverseer autoshutdown() {
        this.autoshutdown = true;
        return this;
    }

    private void describeEventAsync(String nextToken, final ProfileHealth profileHealth, final CompletableFuture<ProfileHealth> future) {
        this.client
                .describeEventsAsync(new DescribeEventsRequest().withFilter(
                        new EventFilter()
                                .withEventStatusCodes(EventStatusCode.Open, EventStatusCode.Upcoming))
                                .withNextToken(nextToken),
                        new AsyncHandler<DescribeEventsRequest, DescribeEventsResult>() {
                            @Override
                            public void onError(Exception exception) {
                                future.completeExceptionally(exception);
                                if(autoshutdown){
                                    shutdown();
                                }
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
                                    describeEventAsync(returnedNextToken, profileHealth, future);
                                }
                                else {
                                    future.complete(profileHealth);
                                    if (autoshutdown){
                                        shutdown();
                                    }
                                }
                            }
                        });
    }


}
