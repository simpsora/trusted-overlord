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

    public <T> T to(ExportTo<T> func){
        return func.export(getOpenIssues(), getScheduledChanges(), getOtherNotifications());
    }

    @FunctionalInterface
    interface ExportTo<T> {
        T export(List<String> openIssues, List<String> scheduledChanges, List<String> otherNotifications);
    }

}
