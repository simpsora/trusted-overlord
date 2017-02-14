package com.beeva.trustedoverlord.clients.tests;

import com.amazonaws.AmazonClientException;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.support.AWSSupportAsync;
import com.amazonaws.services.support.model.*;
import com.beeva.trustedoverlord.clients.SupportClient;
import com.beeva.trustedoverlord.clients.TrustedAdvisorClient;
import com.beeva.trustedoverlord.model.ProfileChecks;
import com.beeva.trustedoverlord.model.ProfileSupportCases;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Beeva
 */
@RunWith(MockitoJUnitRunner.class)
public class TrustedAdvisorClientTest {

    @Mock
    private AWSSupportAsync mockClient;

    @Before
    public void setUp() throws Exception {
        reset(mockClient);
    }

    @Test
    public void testGetProfileChecks() throws Exception {

        // Capture the parameters when invoking the method describeTrustedAdvisorChecksAsync (first callback)
        doAnswer(invocation -> {
            DescribeTrustedAdvisorChecksRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeTrustedAdvisorChecksRequest, DescribeTrustedAdvisorChecksResult> handler =
                    invocation.getArgument(1);

            handler.onSuccess(request, createChecks());

            Assert.assertThat(request.getLanguage(), is(Locale.ENGLISH.getLanguage()));

            return null;
        }).when(mockClient).describeTrustedAdvisorChecksAsync(any(DescribeTrustedAdvisorChecksRequest.class), any());

        // Capture the parameters when invoking the method describeTrustedAdvisorCheckResultAsync (second callback)
        DescribeTrustedAdvisorCheckResultResult describeTrustedAdvisorCheckResultResult = createChecksResult();
        doAnswer(invocation -> {
            DescribeTrustedAdvisorCheckResultRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeTrustedAdvisorCheckResultRequest, DescribeTrustedAdvisorCheckResultResult> handler =
                    invocation.getArgument(1);

            handler.onSuccess(request, describeTrustedAdvisorCheckResultResult);

            Assert.assertThat(request.getLanguage(), is(Locale.ENGLISH.getLanguage()));

            return CompletableFuture.completedFuture(describeTrustedAdvisorCheckResultResult);
        }).when(mockClient).describeTrustedAdvisorCheckResultAsync(any(DescribeTrustedAdvisorCheckResultRequest.class), any());

        // Calls the method
        TrustedAdvisorClient trustedClient = TrustedAdvisorClient.withClient(mockClient);
        CompletableFuture<ProfileChecks> future = (CompletableFuture<ProfileChecks>) trustedClient.getProfileChecks();

        Assert.assertThat(trustedClient.isAutoshutdown(), is(false));

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.get().getErrors().isEmpty(), is(false));
        Assert.assertThat(future.get().getErrors().size(), is(2));
        Assert.assertThat(future.get().getErrors().get(0), is("Name 1"));
        Assert.assertThat(future.get().getExceptions().isEmpty(), is(true));
        Assert.assertThat(future.get().getWarnings().isEmpty(), is(false));
        Assert.assertThat(future.get().getWarnings().size(), is(2));
        Assert.assertThat(future.get().getWarnings().get(1), is("Name 4"));

        verify(mockClient).describeTrustedAdvisorChecksAsync(any(DescribeTrustedAdvisorChecksRequest.class), any());
        verify(mockClient, times(4)).describeTrustedAdvisorCheckResultAsync(any(DescribeTrustedAdvisorCheckResultRequest.class), any());
        verify(mockClient, never()).shutdown();

    }

    private DescribeTrustedAdvisorChecksResult createChecks() {

        TrustedAdvisorCheckDescription check1 = new TrustedAdvisorCheckDescription().withId("check1").withName("Name 1");
        TrustedAdvisorCheckDescription check2 = new TrustedAdvisorCheckDescription().withId("check2").withName("Name 2");
        TrustedAdvisorCheckDescription check3 = new TrustedAdvisorCheckDescription().withId("check3").withName("Name 3");
        TrustedAdvisorCheckDescription check4 = new TrustedAdvisorCheckDescription().withId("check4").withName("Name 4");

        return new DescribeTrustedAdvisorChecksResult().withChecks(check1, check2, check3, check4);
    }

    private DescribeTrustedAdvisorCheckResultResult createChecksResult() {
        TrustedAdvisorCheckResult result1 = new TrustedAdvisorCheckResult().withStatus("error");
        TrustedAdvisorCheckResult result2 = new TrustedAdvisorCheckResult().withStatus("error");
        TrustedAdvisorCheckResult result3 = new TrustedAdvisorCheckResult().withStatus("warning");
        TrustedAdvisorCheckResult result4 = new TrustedAdvisorCheckResult().withStatus("warning");

        DescribeTrustedAdvisorCheckResultResult result = mock(DescribeTrustedAdvisorCheckResultResult.class);
        when(result.getResult()).thenReturn(result1, result2, result3, result4);
        return result;
    }
}
