package com.beeva.trustedoverlord.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by BEEVA
 */
public class ProfileHealth {

    private List<String> openIssues = Collections.synchronizedList(new ArrayList<>());
    private List<String> scheduledChanges = Collections.synchronizedList(new ArrayList<>());
    private List<String> otherNotifications = Collections.synchronizedList(new ArrayList<>());

    public List<String> getOpenIssues() {
        return openIssues;
    }

    public List<String> getScheduledChanges() {
        return scheduledChanges;
    }

    public List<String> getOtherNotifications() {
        return otherNotifications;
    }

    public void addOpenIssue(final String openIssue) {
        this.openIssues.add(openIssue);
    }

    public void addScheduledChange(final String scheduledChange) {
        this.scheduledChanges.add(scheduledChange);
    }

    public void addOtherNotifications(final String otherNotifications) {
        this.otherNotifications.add(otherNotifications);
    }

    public String toMarkdown() {

        StringBuffer result = new StringBuffer("#### __Health Dashboard__\n");
        openIssues.forEach(openIssue ->
                result.append("* __Open Issue:__ ").append(openIssue).append("\n"));
        scheduledChanges.forEach(scheduledChange ->
                result.append("* __Scheduled Change:__ ").append(scheduledChange).append("\n"));
        otherNotifications.forEach(otherNotification ->
                result.append("* __Other Notification:__ ").append(otherNotification).append("\n"));
        return result.toString();

    }

}
