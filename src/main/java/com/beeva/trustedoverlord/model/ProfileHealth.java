package com.beeva.trustedoverlord.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cesarsilgo on 7/02/17.
 */
public class ProfileHealth {

    private List<String> openIssues;
    private List<String> scheduledChanges;
    private List<String> otherNotifications;

    public ProfileHealth() {
        openIssues = new ArrayList<>();
        scheduledChanges = new ArrayList<>();
        otherNotifications = new ArrayList<>();
    }

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

}
