package com.beeva.trustedoverlord.model;

import com.amazonaws.services.health.model.AWSHealthException;
import com.amazonaws.services.support.model.AWSSupportException;
import com.beeva.trustedoverlord.TrustedOverlordClientFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

/**
 * Created by BEEVA
 */
public class Profile {

    private static Logger logger = LogManager.getLogger(Profile.class);

    private String profileName;
    private ProfileChecks profileChecks = new ProfileChecks();
    private ProfileHealth profileHealth = new ProfileHealth();
    private ProfileSupportCases profileSupportCases = new ProfileSupportCases();

    public Profile(String profileName) {

        this.profileName = profileName;

        try {

            try {
                this.profileChecks = TrustedOverlordClientFactory.trustedAdvisorApi().clientWithProfile(profileName)
                        .autoshutdown().getProfileChecks().get();
            } catch (AWSSupportException e) {
                logger.error(e.getErrorMessage());
            }

            try {
                this.profileHealth = TrustedOverlordClientFactory.healthApi().clientWithProfile(profileName)
                        .autoshutdown().getProfileHealth().get();
            } catch (AWSHealthException e) {
                logger.error(e.getErrorMessage());
            }

            try {
                this.profileSupportCases = TrustedOverlordClientFactory.supportApi().clientWithProfile(profileName)
                        .autoshutdown().getSupportCases().get();
            } catch (AWSSupportException e) {
                logger.error(e.getErrorMessage());
            }

        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
        }
    }

    public String getProfileName() {
        return profileName;
    }

    public ProfileChecks getProfileChecks() {
        return profileChecks;
    }

    public ProfileHealth getProfileHealth() {
        return profileHealth;
    }

    public ProfileSupportCases getProfileSupportCases() {
        return profileSupportCases;
    }

    public String toMarkdown() {

        String pChecks = profileChecks.to(
                (errors, warnings, exceptions) -> {
                    StringBuffer result = new StringBuffer();
                    errors.forEach(error -> result.append("* __Error:__ ").append(error).append("\n"));
                    warnings.forEach(warning -> result.append("* __Warning:__ ").append(warning).append("\n"));
                    exceptions.forEach(exception -> result.append("* __Exception:__ ").append(exception).append("\n"));
                    return result.toString();
                }
        );

        String pHealth = profileHealth.to(
                (openIssues, scheduledChanges, otherNotifications) -> {
                    StringBuffer result = new StringBuffer();
                    openIssues.forEach(openIssue -> result.append("* __Open Issue:__ ").append(openIssue).append("\n"));
                    scheduledChanges.forEach(scheduledChange -> result.append("* __Scheduled Change:__ ").append(scheduledChange).append("\n"));
                    otherNotifications.forEach(otherNotification -> result.append("* __Other Notification:__ ").append(otherNotification).append("\n"));
                    return result.toString();
                }
        );

        String pSupport = profileSupportCases.to(
                (openCases, resolvedCases) -> {
                    StringBuffer result = new StringBuffer();
                    openCases.forEach(openCase -> result.append("* __Open Case:__ ").append(openCase.toString()).append("\n"));
                    resolvedCases.forEach(resolvedCase -> result.append("* __Resolved Case:__ ").append(resolvedCase.toString()).append("\n"));
                    return result.toString();
                }
        );

        return new StringBuffer("## __").append(profileName).append("__\n")
                .append("#### __Trusted Advisor__\n")
                .append(pChecks)
                .append("#### __Health Dashboard__\n")
                .append(pHealth)
                .append("#### __Support Cases__\n")
                .append(pSupport)
                .append("\n---\n").toString();

    }

}
