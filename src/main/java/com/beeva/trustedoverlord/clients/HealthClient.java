package com.beeva.trustedoverlord.clients;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.health.AWSHealthAsync;
import com.amazonaws.services.health.AWSHealthAsyncClientBuilder;
import com.amazonaws.services.health.model.*;
import com.beeva.trustedoverlord.model.ProfileHealth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Created by Beeva
 */
public class HealthClient implements Client {

    private AWSHealthAsync client;
    private boolean autoshutdown = false;
    private static Logger logger = LogManager.getLogger(HealthClient.class);



    public HealthClient(String profile) {
        this(AWSHealthAsyncClientBuilder
                .standard()
                    .withCredentials(new ProfileCredentialsProvider(profile))
                    .withRegion(Regions.US_EAST_1.getName())
                .build());
    }

    private HealthClient(AWSHealthAsync client){
        this.client = client;
    }

    public Future<ProfileHealth> getProfileHealth() {

        ProfileHealth profileHealth = new ProfileHealth();
        CompletableFuture<ProfileHealth> future = new CompletableFuture<>();

        describeEventAsync(null, profileHealth, future);

        return future;

    }

    public static HealthClient withClient(AWSHealthAsync client) {
        if (client == null){
            return new HealthClient(
                        AWSHealthAsyncClientBuilder.standard()
                                .withRegion(Regions.US_EAST_1.getName())
                                .build()
                        );
        }
        else {
            return new HealthClient(client);
        }
    }

    @Override
    public void shutdown() {
        this.client.shutdown();
    }

    @Override
    @SuppressWarnings("unchecked")
    public HealthClient autoshutdown() {
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
                                    logger.debug(event.toString());
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
