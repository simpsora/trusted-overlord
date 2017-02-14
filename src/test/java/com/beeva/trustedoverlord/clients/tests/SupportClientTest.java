package com.beeva.trustedoverlord.clients.tests;

import com.amazonaws.AmazonClientException;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.support.AWSSupportAsync;
import com.amazonaws.services.support.model.CaseDetails;
import com.amazonaws.services.support.model.DescribeCasesRequest;
import com.amazonaws.services.support.model.DescribeCasesResult;
import com.beeva.trustedoverlord.clients.SupportClient;
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
public class SupportClientTest {

    @Mock
    private AWSSupportAsync mockClient;

    @Before
    public void setUp() throws Exception {
        reset(mockClient);
    }

    @Test
    public void testGetProfileSupportCases() throws Exception {

        // Capture the parameters when invoking the method describeCasesAsync
        doAnswer(invocation -> {
            DescribeCasesRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeCasesRequest, DescribeCasesResult> handler = invocation.getArgument(1);

            handler.onSuccess(request, createCaseDetails());

            Assert.assertThat(request.getIncludeResolvedCases(), is(false));

            return null;
        }).when(mockClient).describeCasesAsync(any(DescribeCasesRequest.class), any());

        // Calls the method
        SupportClient supportClient = SupportClient.withClient(mockClient);
        CompletableFuture<ProfileSupportCases> future = (CompletableFuture<ProfileSupportCases>) supportClient.getSupportCases();

        Assert.assertThat(supportClient.isAutoshutdown(), is(false));

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.get().getOpenCases().isEmpty(), is(false));
        Assert.assertThat(future.get().getResolvedCases().isEmpty(), is(true));

        verify(mockClient).describeCasesAsync(any(DescribeCasesRequest.class), any());
        verify(mockClient, never()).shutdown();

    }

    /**
     * Verifies that the method is invoked recursively twice to retrieve the whole data
     */
    @Test
    public void testGetProfileSupportCasesWithToken() throws Exception {

        DescribeCasesResult describeCasesResult = createCaseDetailsWithToken();

        // Capture the parameters when invoking the method describeCasesAsync
        doAnswer(invocation -> {
            DescribeCasesRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeCasesRequest, DescribeCasesResult> handler = invocation.getArgument(1);

            handler.onSuccess(request, describeCasesResult);

            Assert.assertThat(request.getIncludeResolvedCases(), is(false));

            return null;
        }).when(mockClient).describeCasesAsync(any(DescribeCasesRequest.class), any());

        // Calls the method
        CompletableFuture<ProfileSupportCases> future = (CompletableFuture<ProfileSupportCases>)SupportClient.withClient(mockClient).getSupportCases();

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.get().getOpenCases().isEmpty(), is(false));
        Assert.assertThat(future.get().getResolvedCases().isEmpty(), is(true));

        verify(mockClient, times(2)).describeCasesAsync(any(DescribeCasesRequest.class), any());
        verify(mockClient, never()).shutdown();

    }

    @Test
    public void testGetProfileSupportCasesAWSEmptyReturnData() throws Exception {

        // Capture the parameters when invoking the method describeCasesAsync
        doAnswer(invocation -> {
            DescribeCasesRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeCasesRequest, DescribeCasesResult> handler = invocation.getArgument(1);

            handler.onSuccess(request, new DescribeCasesResult().withCases(Collections.emptyList()));

            Assert.assertThat(request.getIncludeResolvedCases(), is(false));

            return null;
        }).when(mockClient).describeCasesAsync(any(DescribeCasesRequest.class), any());

        // Calls the method
        SupportClient supportClient = SupportClient.withClient(mockClient);
        CompletableFuture<ProfileSupportCases> future = (CompletableFuture<ProfileSupportCases>) supportClient.getSupportCases();

        Assert.assertThat(supportClient.isAutoshutdown(), is(false));

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.get().getOpenCases().isEmpty(), is(true));
        Assert.assertThat(future.get().getResolvedCases().isEmpty(), is(true));

        verify(mockClient).describeCasesAsync(any(DescribeCasesRequest.class), any());
        verify(mockClient, never()).shutdown();

    }

    @Test
    public void testGetProfileSupportCasesThrowException() throws Exception {

        // Capture the parameters when invoking the method describeCasesAsync
        doAnswer(invocation -> {
            DescribeCasesRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeCasesRequest, DescribeCasesResult> handler = invocation.getArgument(1);

            handler.onError(new AmazonClientException("testException"));

            Assert.assertThat(request.getIncludeResolvedCases(), is(false));

            return null;
        }).when(mockClient).describeCasesAsync(any(DescribeCasesRequest.class), any());

        // Calls the method
        SupportClient supportClient = SupportClient.withClient(mockClient);
        CompletableFuture<ProfileSupportCases> future = (CompletableFuture<ProfileSupportCases>) supportClient.getSupportCases();

        Assert.assertThat(supportClient.isAutoshutdown(), is(false));

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.isCompletedExceptionally(), is(true));

        verify(mockClient).describeCasesAsync(any(DescribeCasesRequest.class), any());
        verify(mockClient, never()).shutdown();

        try{
            future.get(); //This should throw an exception
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertThat(e, instanceOf(ExecutionException.class));
            Assert.assertThat(e.getCause(), instanceOf(AmazonClientException.class));
        }

    }

    @Test
    public void testGetProfileSupportCasesAutoshutdown() throws Exception {

        // Capture the parameters when invoking the method describeCasesAsync
        doAnswer(invocation -> {
            DescribeCasesRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeCasesRequest, DescribeCasesResult> handler = invocation.getArgument(1);

            handler.onSuccess(request, createCaseDetails());

            Assert.assertThat(request.getIncludeResolvedCases(), is(false));

            return null;
        }).when(mockClient).describeCasesAsync(any(DescribeCasesRequest.class), any());

        // Calls the method
        SupportClient supportClient = SupportClient.withClient(mockClient).autoshutdown();
        CompletableFuture<ProfileSupportCases> future = (CompletableFuture<ProfileSupportCases>) supportClient.getSupportCases();

        Assert.assertThat(supportClient.isAutoshutdown(), is(true));

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.get().getOpenCases().isEmpty(), is(false));
        Assert.assertThat(future.get().getResolvedCases().isEmpty(), is(true));

        verify(mockClient).describeCasesAsync(any(DescribeCasesRequest.class), any());
        verify(mockClient).shutdown();

    }

    @Test
    public void testGetProfileSupportCasesThrowExceptionAutoshutdown() throws Exception {

        // Capture the parameters when invoking the method describeCasesAsync
        doAnswer(invocation -> {
            DescribeCasesRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeCasesRequest, DescribeCasesResult> handler = invocation.getArgument(1);

            handler.onError(new AmazonClientException("testException"));

            Assert.assertThat(request.getIncludeResolvedCases(), is(false));

            return null;
        }).when(mockClient).describeCasesAsync(any(DescribeCasesRequest.class), any());

        // Calls the method
        SupportClient supportClient = SupportClient.withClient(mockClient).autoshutdown();
        CompletableFuture<ProfileSupportCases> future = (CompletableFuture<ProfileSupportCases>) supportClient.getSupportCases();

        Assert.assertThat(supportClient.isAutoshutdown(), is(true));

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.isCompletedExceptionally(), is(true));

        verify(mockClient).describeCasesAsync(any(DescribeCasesRequest.class), any());
        verify(mockClient).shutdown();

        try{
            future.get(); //This should throw an exception
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertThat(e, instanceOf(ExecutionException.class));
            Assert.assertThat(e.getCause(), instanceOf(AmazonClientException.class));
        }

    }

    private DescribeCasesResult createCaseDetails(){
        CaseDetails case1 = new CaseDetails().withCaseId("case1").withTimeCreated(LocalDateTime.now().toString())
                                             .withStatus("test1").withSubject("test Subject 1").withSubmittedBy("test@test.com");
        CaseDetails case2 = new CaseDetails().withCaseId("case2").withTimeCreated(LocalDateTime.now().toString())
                                             .withStatus("test2").withSubject("test Subject 2").withSubmittedBy("test@test.com");
        CaseDetails case3 = new CaseDetails().withCaseId("case3").withTimeCreated(LocalDateTime.now().toString())
                                             .withStatus("test3").withSubject("test Subject 3").withSubmittedBy("test@test.com");

        return new DescribeCasesResult().withCases(case1, case2, case3);
    }

    private DescribeCasesResult createCaseDetailsWithToken(){
        DescribeCasesResult result = spy(createCaseDetails());

        when(result.getNextToken()).thenReturn("first", "");

        return result;
    }
}
