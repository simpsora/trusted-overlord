package com.beeva.trustedoverlord.clients.tests;

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

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
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

            handler.onSuccess(request, createDescribeEventsResultWithThreeEvents());

            Assert.assertThat(request.getFilter().getEventStatusCodes(), hasItems("open", "upcoming"));

            return null;
        }).when(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());

        // Calls the method
        CompletableFuture<ProfileHealth> future = (CompletableFuture<ProfileHealth>)HealthClient.withClient(mockClient).getProfileHealth();

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));

        verify(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());

    }

    @Test
    public void testGetProfileHealthWithToken() throws Exception {

        DescribeEventsResult describeEventsResult = createDescribeEventsResultWithThreeEventsWithToken();

        // Capture the parameters when invoking the method describeEventsAsync
        doAnswer(invocation -> {
            DescribeEventsRequest request = invocation.getArgument(0);
            AsyncHandler<DescribeEventsRequest, DescribeEventsResult> handler = invocation.getArgument(1);

            handler.onSuccess(request, describeEventsResult);

            Assert.assertThat(request.getFilter().getEventStatusCodes(), hasItems("open", "upcoming"));

            return null;
        }).when(mockClient).describeEventsAsync(any(DescribeEventsRequest.class), any());

        // Calls the method
        CompletableFuture<ProfileHealth> future = (CompletableFuture<ProfileHealth>)HealthClient.withClient(mockClient).getProfileHealth();

        // Waits until the future is complete
        Awaitility.await().until(future::isDone);

        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));
        Assert.assertThat(future.get().getOpenIssues().isEmpty(), is(false));

        verify(mockClient, times(2)).describeEventsAsync(any(DescribeEventsRequest.class), any());

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
