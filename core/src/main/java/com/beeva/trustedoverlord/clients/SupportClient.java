package com.beeva.trustedoverlord.clients;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.auth.profile.internal.BasicProfile;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.support.AWSSupportAsync;
import com.amazonaws.services.support.AWSSupportAsyncClientBuilder;
import com.amazonaws.services.support.model.*;
import com.beeva.trustedoverlord.model.ProfileSupportCases;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

/**
 * Created by Beeva
 */
public class SupportClient implements Client {

    private AWSSupportAsync client;
    private boolean autoshutdown = false;
    private static Logger logger = LogManager.getLogger(SupportClient.class);



    public SupportClient(String profile) {
        this(AWSSupportAsyncClientBuilder
                .standard()
                    .withCredentials(getCredentialsProvider(profile))
                    .withRegion(Regions.US_EAST_1.getName())
                .build());
    }

    private SupportClient(AWSSupportAsync client){
        this.client = client;
    }

    private static AWSCredentialsProvider getCredentialsProvider(String profile) {
        BasicProfile basicProfile = new ProfilesConfigFile().getAllBasicProfiles().get(profile);
        if(basicProfile == null) {
            throw new RuntimeException("No AWS profile named '" + profile + "'");
        }

        if (basicProfile.isRoleBasedProfile()) {
           return new STSAssumeRoleSessionCredentialsProvider.Builder(basicProfile.getRoleArn(), "supportClientSession")
                       .withStsClient(AWSSecurityTokenServiceClientBuilder.defaultClient()).build();
        } else {
            return new ProfileCredentialsProvider(profile);
        }
    }

    public CompletableFuture<ProfileSupportCases> getSupportCases() {
        ProfileSupportCases cases = new ProfileSupportCases();
        CompletableFuture<ProfileSupportCases> future = new CompletableFuture<>();

        describeCases(null, cases, future);

        return future;

    }

    public static SupportClient withClient(AWSSupportAsync client) {
        if (client == null){
            return new SupportClient(
                            AWSSupportAsyncClientBuilder.standard()
                                .withRegion(Regions.US_EAST_1.getName())
                                .build()
                        );
        }
        else {
            return new SupportClient(client);
        }
    }

    @Override
    public void shutdown() {
        this.client.shutdown();
    }

    @Override
    @SuppressWarnings("unchecked")
    public SupportClient autoshutdown() {
        this.autoshutdown = true;
        return this;
    }

    @Override
    public boolean isAutoshutdown() {
        return this.autoshutdown;
    }

    private void describeCases(String nextToken, final ProfileSupportCases cases, final CompletableFuture<ProfileSupportCases> future) {
        this.client
                .describeCasesAsync(new DescribeCasesRequest()
                                .withIncludeResolvedCases(false)
                                .withIncludeCommunications(false)
                                .withNextToken(nextToken),
                        new AsyncHandler<DescribeCasesRequest, DescribeCasesResult>() {
                            @Override
                            public void onError(Exception exception) {
                                future.completeExceptionally(exception);
                                if(autoshutdown){
                                    shutdown();
                                }
                            }

                            @Override
                            public void onSuccess(DescribeCasesRequest request, DescribeCasesResult describeCasesResult) {
                                describeCasesResult.getCases().forEach(caseDetails -> {
                                    logger.debug(caseDetails.toString());
                                    cases.addOpenCase(
                                            caseDetails.getCaseId(), caseDetails.getTimeCreated(),
                                            caseDetails.getStatus(), caseDetails.getSubmittedBy(), caseDetails.getSubject()
                                    );
                                });

                                String returnedNextToken = describeCasesResult.getNextToken();
                                if (returnedNextToken != null && !returnedNextToken.isEmpty()){
                                    describeCases(returnedNextToken, cases, future);
                                }
                                else {
                                    future.complete(cases);
                                    if (autoshutdown){
                                        shutdown();
                                    }
                                }
                            }
                        });
    }
}
