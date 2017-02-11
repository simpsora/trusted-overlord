package com.beeva.trustedoverlord.overseers;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.support.AWSSupportAsync;
import com.amazonaws.services.support.AWSSupportAsyncClientBuilder;
import com.amazonaws.services.support.model.*;
import com.beeva.trustedoverlord.model.ProfileChecks;
import com.beeva.trustedoverlord.model.ProfileSupportCases;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

/**
 * Created by Beeva
 */
public class SupportOverseer implements Overseer{

    private AWSSupportAsync client;
    private boolean autoshutdown = false;


    public SupportOverseer(String profile) {
        this(AWSSupportAsyncClientBuilder
                .standard()
                    .withCredentials(new ProfileCredentialsProvider(profile))
                    .withRegion(Regions.US_EAST_1.getName())
                .build());
    }

    private SupportOverseer(AWSSupportAsync client){
        this.client = client;
    }

    public Future<ProfileSupportCases> getSupportCases() {
        ProfileSupportCases cases = new ProfileSupportCases();
        CompletableFuture<ProfileSupportCases> future = new CompletableFuture<>();

        describeCases(null, cases, future);

        return future;

    }

    public static SupportOverseer withClient(AWSSupportAsync client) {
        if (client == null){
            return new SupportOverseer(
                            AWSSupportAsyncClientBuilder.standard()
                                .withRegion(Regions.US_EAST_1.getName())
                                .build()
                        );
        }
        else {
            return new SupportOverseer(client);
        }
    }

    @Override
    public void shutdown() {
        this.client.shutdown();
    }

    @Override
    @SuppressWarnings("unchecked")
    public SupportOverseer autoshutdown() {
        this.autoshutdown = true;
        return this;
    }

    private void describeCases(String nextToken, final ProfileSupportCases cases, final CompletableFuture<ProfileSupportCases> future) {
        this.client
                .describeCasesAsync(new DescribeCasesRequest()
                                .withIncludeResolvedCases(true)
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
                                    if (!"resolved".equalsIgnoreCase(caseDetails.getStatus())){
                                        cases.addOpenCase(
                                                caseDetails.getCaseId(), caseDetails.getTimeCreated(),
                                                caseDetails.getStatus(), caseDetails.getSubmittedBy(), caseDetails.getSubject()
                                        );
                                    }
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
