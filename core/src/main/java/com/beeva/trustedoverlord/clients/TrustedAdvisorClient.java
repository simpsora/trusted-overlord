package com.beeva.trustedoverlord.clients;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.support.AWSSupportAsync;
import com.amazonaws.services.support.AWSSupportAsyncClientBuilder;
import com.amazonaws.services.support.model.DescribeTrustedAdvisorCheckResultRequest;
import com.amazonaws.services.support.model.DescribeTrustedAdvisorCheckResultResult;
import com.amazonaws.services.support.model.DescribeTrustedAdvisorChecksRequest;
import com.amazonaws.services.support.model.DescribeTrustedAdvisorChecksResult;
import com.beeva.trustedoverlord.model.ProfileChecks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

/**
 * Created by Beeva
 */
public class TrustedAdvisorClient implements Client {

    private AWSSupportAsync client;
    private boolean autoshutdown = false;
    private static Logger logger = LogManager.getLogger(TrustedAdvisorClient.class);



    public TrustedAdvisorClient(String profile) {
        this(AWSSupportAsyncClientBuilder
                .standard()
                    .withRegion(Regions.US_EAST_1.getName())
                .build());
    }

    private TrustedAdvisorClient(AWSSupportAsync client){
        this.client = client;
    }

    public CompletableFuture<ProfileChecks> getProfileChecks() {

        CompletableFuture<ProfileChecks> future = new CompletableFuture<>();

        ProfileChecks checks = new ProfileChecks();

        this.client
                .describeTrustedAdvisorChecksAsync(
                        new DescribeTrustedAdvisorChecksRequest().withLanguage(Locale.ENGLISH.getLanguage()),
                        new TrustedAdvisorChecksResultHandler(checks, future)
                );

        return future;

    }

    public static TrustedAdvisorClient withClient(AWSSupportAsync client) {
        if (client == null){
            return new TrustedAdvisorClient(
                            AWSSupportAsyncClientBuilder.standard()
                                .withRegion(Regions.US_EAST_1.getName())
                                .build()
                        );
        }
        else {
            return new TrustedAdvisorClient(client);
        }
    }

    @Override
    public void shutdown() {
        this.client.shutdown();
    }

    @Override
    @SuppressWarnings("unchecked")
    public TrustedAdvisorClient autoshutdown() {
        this.autoshutdown = true;
        return this;
    }

    @Override
    public boolean isAutoshutdown() {
        return this.autoshutdown;
    }

    private class TrustedAdvisorChecksResultHandler implements AsyncHandler<DescribeTrustedAdvisorChecksRequest, DescribeTrustedAdvisorChecksResult> {

        private ProfileChecks profileChecks;
        private CompletableFuture<ProfileChecks> profileChecksFuture;

        TrustedAdvisorChecksResultHandler(ProfileChecks profileChecks, CompletableFuture<ProfileChecks> profileChecksFuture) {
            this.profileChecks = profileChecks;
            this.profileChecksFuture = profileChecksFuture;
        }

        @Override
        public void onError(Exception e) {
            profileChecksFuture.completeExceptionally(e);
            if(autoshutdown){
                shutdown();
            }
        }

        @Override
        public void onSuccess(DescribeTrustedAdvisorChecksRequest request,
                              DescribeTrustedAdvisorChecksResult describeTrustedAdvisorChecksResult) {

            List<Future<DescribeTrustedAdvisorCheckResultResult>> futures = new ArrayList<>();

            describeTrustedAdvisorChecksResult.getChecks()
                    .forEach(checkDescription ->
                        futures.add(
                            client.describeTrustedAdvisorCheckResultAsync(
                                new DescribeTrustedAdvisorCheckResultRequest()
                                        .withCheckId(checkDescription.getId())
                                        .withLanguage(Locale.ENGLISH.getLanguage()),
                                new AsyncHandler<DescribeTrustedAdvisorCheckResultRequest, DescribeTrustedAdvisorCheckResultResult>() {
                                    @Override
                                    public void onError(Exception exception) {profileChecks.addException(exception);}

                                    @Override
                                    public void onSuccess(DescribeTrustedAdvisorCheckResultRequest request,
                                                          DescribeTrustedAdvisorCheckResultResult describeTrustedAdvisorCheckResult) {
                                        if ("error".equals(describeTrustedAdvisorCheckResult.getResult().getStatus())) {
                                            logger.debug(describeTrustedAdvisorCheckResult.toString());
                                            profileChecks.addError(checkDescription.getName());
                                        } else if ("warning".equals(describeTrustedAdvisorCheckResult.getResult().getStatus())) {
                                            logger.debug(describeTrustedAdvisorCheckResult.toString());
                                            profileChecks.addWarning(checkDescription.getName());
                                        }
                                    }
                                }
                            )
                        )
                    );

            waitForFuturesToComplete(futures);

            this.profileChecksFuture.complete(this.profileChecks);
            if (autoshutdown){
                shutdown();
            }
        }

        private void waitForFuturesToComplete(List<Future<DescribeTrustedAdvisorCheckResultResult>> futures) {
            futures.parallelStream().forEach(future -> {
                try {
                    future.get(20, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    logger.error(e);
                }
            });
        }
    }
}
