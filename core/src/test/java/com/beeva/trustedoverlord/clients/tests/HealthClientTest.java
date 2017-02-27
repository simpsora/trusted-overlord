package com.beeva.trustedoverlord.clients.tests;

import com.amazonaws.AmazonClientException;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.health.AWSHealthAsync;
import com.amazonaws.services.health.model.DescribeEventsRequest;
import com.amazonaws.services.health.model.DescribeEventsResult;
import com.amazonaws.services.health.model.Event;
import com.amazonaws.services.health.model.EventTypeCategory;
import com.beeva.trustedoverlord.clients.HealthClient;
import com.beeva.trustedoverlord.model.ProfileHealth;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Beeva
 */
@RunWith(MockitoJUnitRunner.class)
public class HealthClientTest {

    @Mock
    private AWSHealthAsync mockClient;

    @Before
    public void setUp() throws Exception {
        reset(mockClient);
    }

    @Test
    public void testGetProfileHealth() throws Exception {

        // Capture the parameters when invoking the method describeEventsAsync
        doAnswer(invocation -> {
            DescribeEventsRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeEventsRequest, DescribeEventsResult> handler = invocation.getArgument(1);

            DescribeEventsResult result = createDescribeEventsResultWithThreeEvents();
            handler.onSuccess(request, result);

            Assert.assertThat(request.getFilter().getEventStatusCodes(), hasItems("open", "upcoming"));

            return CompletableFuture.completedFuture(result);
        }).when(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());

        // Calls the method
        HealthClient healthClient = HealthClient.withClient(mockClient);
        CompletableFuture<ProfileHealth> future = (CompletableFuture<ProfileHealth>) healthClient.getProfileHealth();

        Assert.assertThat(healthClient.isAutoshutdown(), is(false));

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));

        verify(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());
        verify(mockClient, never()).shutdown();

    }

    /**
     * Verifies that the method is invoked recursively twice to retrieve the whole data
     */
    @Test
    public void testGetProfileHealthWithToken() throws Exception {

        DescribeEventsResult describeEventsResult = createDescribeEventsResultWithThreeEventsWithToken();

        // Capture the parameters when invoking the method describeEventsAsync
        doAnswer(invocation -> {
            DescribeEventsRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeEventsRequest, DescribeEventsResult> handler = invocation.getArgument(1);

            handler.onSuccess(request, describeEventsResult);

            Assert.assertThat(request.getFilter().getEventStatusCodes(), hasItems("open", "upcoming"));

            return CompletableFuture.completedFuture(describeEventsResult);
        }).when(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());

        // Calls the method
        CompletableFuture<ProfileHealth> future = (CompletableFuture<ProfileHealth>)HealthClient.withClient(mockClient).getProfileHealth();

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));

        verify(mockClient, times(2)).describeEventsAsync(any(DescribeEventsRequest.class), any());
        verify(mockClient, never()).shutdown();

    }

    @Test
    public void testGetProfileHealthAWSEmptyReturnData() throws Exception {

        // Capture the parameters when invoking the method describeEventsAsync
        doAnswer(invocation -> {
            DescribeEventsRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeEventsRequest, DescribeEventsResult> handler = invocation.getArgument(1);

            handler.onSuccess(request, new DescribeEventsResult().withEvents(Collections.emptyList()));

            Assert.assertThat(request.getFilter().getEventStatusCodes(), hasItems("open", "upcoming"));

            return CompletableFuture.completedFuture(new DescribeEventsResult().withEvents(Collections.emptyList()));
        }).when(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());

        // Calls the method
        CompletableFuture<ProfileHealth> future = (CompletableFuture<ProfileHealth>)HealthClient.withClient(mockClient).getProfileHealth();

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(true));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(true));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(true));

        verify(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());
        verify(mockClient, never()).shutdown();

    }

    @Test
    public void testGetProfileHealthThrowException() throws Exception {

        // Capture the parameters when invoking the method describeEventsAsync
        doAnswer(invocation -> {
            DescribeEventsRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeEventsRequest, DescribeEventsResult> handler = invocation.getArgument(1);

            AmazonClientException testException = new AmazonClientException("testException");
            handler.onError(testException);

            Assert.assertThat(request.getFilter().getEventStatusCodes(), hasItems("open", "upcoming"));

            return CompletableFuture.completedFuture(null);
        }).when(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());

        // Calls the method
        CompletableFuture<ProfileHealth> future = (CompletableFuture<ProfileHealth>)HealthClient.withClient(mockClient).getProfileHealth();

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.isCompletedExceptionally(), is(true));

        verify(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());
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
    public void testGetProfileHealthAutoshutdown() throws Exception {

        // Capture the parameters when invoking the method describeEventsAsync
        doAnswer(invocation -> {
            DescribeEventsRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeEventsRequest, DescribeEventsResult> handler = invocation.getArgument(1);

            DescribeEventsResult result = createDescribeEventsResultWithThreeEvents();
            handler.onSuccess(request, result);

            Assert.assertThat(request.getFilter().getEventStatusCodes(), hasItems("open", "upcoming"));

            return CompletableFuture.completedFuture(result);
        }).when(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());

        // Calls the method
        HealthClient healthClient = HealthClient.withClient(mockClient).autoshutdown();
        CompletableFuture<ProfileHealth> future = (CompletableFuture<ProfileHealth>) healthClient.getProfileHealth();

        Assert.assertThat(healthClient.isAutoshutdown(), is(true));

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));

        verify(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());
        verify(mockClient).shutdown();

    }


    @Test
    public void testGetProfileHealthThrowExceptionAutoshutdown() throws Exception {

        // Capture the parameters when invoking the method describeEventsAsync
        doAnswer(invocation -> {
            DescribeEventsRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeEventsRequest, DescribeEventsResult> handler = invocation.getArgument(1);

            handler.onError(new AmazonClientException("testException"));

            Assert.assertThat(request.getFilter().getEventStatusCodes(), hasItems("open", "upcoming"));

            return CompletableFuture.completedFuture(null);
        }).when(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());

        // Calls the method
        HealthClient healthClient = HealthClient.withClient(mockClient).autoshutdown();
        CompletableFuture<ProfileHealth> future = (CompletableFuture<ProfileHealth>) healthClient.getProfileHealth();

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.isCompletedExceptionally(), is(true));

        verify(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());
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

    private DescribeEventsResult createDescribeEventsResultWithThreeEvents(){
        Event eventIssue = new Event().withEventTypeCategory(EventTypeCategory.Issue).withEventTypeCode("issue");
        Event eventAccount = new Event().withEventTypeCategory(EventTypeCategory.AccountNotification).withEventTypeCode("notification");
        Event eventScheduled = new Event().withEventTypeCategory(EventTypeCategory.ScheduledChange).withEventTypeCode("scheduled");

        return new DescribeEventsResult().withEvents(eventIssue, eventAccount, eventScheduled);
    }

    private DescribeEventsResult createDescribeEventsResultWithThreeEventsWithToken(){
        DescribeEventsResult result = spy(createDescribeEventsResultWithThreeEvents());

        when(result.getNextToken()).thenReturn("first", "");

        return result;
    }
}
