package com.beeva.trustedoverlord.model;

import com.amazonaws.services.health.model.AWSHealthException;
import com.amazonaws.services.support.model.AWSSupportException;
import com.beeva.trustedoverlord.TrustedOverlordClientFactory;
import com.beeva.trustedoverlord.utils.BannerLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Collects the results of the invocation to the AWS APIs: TrustedAdvisor, Health and Support
 * for a single profile
 *
 * Created by BEEVA
 */
public class ProfileCollector {

    private static Logger logger = LogManager.getLogger(ProfileCollector.class);
    private static Logger banner = BannerLogger.getLogger();

    private String profileName;
    private CompletableFuture<ProfileChecks> profileChecksFuture;
    private CompletableFuture<ProfileHealth> profileHealthFuture;
    private CompletableFuture<ProfileSupportCases> profileSupportCasesFuture;

    public ProfileCollector(String profileName) {

        logger.info("Retrieving information for profile: '{}'...", profileName);

        this.profileName = profileName;

        this.profileChecksFuture = TrustedOverlordClientFactory.trustedAdvisorApi()
                .clientWithProfile(this.profileName)
                .autoshutdown()
                .getProfileChecks();

        this.profileHealthFuture = TrustedOverlordClientFactory.healthApi()
                .clientWithProfile(this.profileName)
                .autoshutdown()
                .getProfileHealth();

        this.profileSupportCasesFuture = TrustedOverlordClientFactory.supportApi()
                .clientWithProfile(this.profileName)
                .autoshutdown()
                .getSupportCases();
    }

    public String getProfileName() {
        return this.profileName;
    }

    public ProfileChecks getProfileChecks() throws ExecutionException, InterruptedException {
        try{
            return this.profileChecksFuture.get();
        } catch (AWSSupportException e) {
            logger.error(e.getErrorMessage());
            return new ProfileChecks();
        }
    }

    public ProfileHealth getProfileHealth() throws ExecutionException, InterruptedException {
        try {
            return this.profileHealthFuture.get();
        } catch (AWSHealthException e) {
            logger.error(e.getErrorMessage());
            return new ProfileHealth();
        }
    }

    public ProfileSupportCases getProfileSupportCases() throws ExecutionException, InterruptedException {
        try {
            return this.profileSupportCasesFuture.get();
        } catch (AWSSupportException e) {
            logger.error(e.getErrorMessage());
            return new ProfileSupportCases();
        }
    }

    /**
     * Exports the results in a String Formatted with Markdown syntax
     */
    public String toMarkdown() {

        try {
            String pChecks = getProfileChecks().to(
                    (errors, warnings, exceptions) -> {
                        StringBuffer result = new StringBuffer();
                        errors.forEach(error -> result.append("* __Error:__ ").append(error).append("\n"));
                        warnings.forEach(warning -> result.append("* __Warning:__ ").append(warning).append("\n"));
                        exceptions.forEach(exception -> result.append("* __Exception:__ ").append(exception).append("\n"));
                        return result.toString();
                    }
            );

            String pHealth = getProfileHealth().to(
                    (openIssues, scheduledChanges, otherNotifications) -> {
                        StringBuffer result = new StringBuffer();
                        openIssues.forEach(openIssue -> result.append("* __Open Issue:__ ").append(openIssue).append("\n"));
                        scheduledChanges.forEach(scheduledChange -> result.append("* __Scheduled Change:__ ").append(scheduledChange).append("\n"));
                        otherNotifications.forEach(otherNotification -> result.append("* __Other Notification:__ ").append(otherNotification).append("\n"));
                        return result.toString();
                    }
            );

            String pSupport = getProfileSupportCases().to(
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

        } catch (ExecutionException | InterruptedException e) {
            return new StringBuffer("## __").append(profileName).append("__\n")
                    .append(e.getMessage())
                    .append("\n---\n").toString();
        }
    }

    /**
     * Prints the results using a specific Logger
     */
    public void toLogger(Logger logger) {

        try {
            ProfileHealth profileHealth = getProfileHealth();

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking Health for profile '{}'", getProfileName());
            banner.info("=====================================================================");

            profileHealth.to(
                    (openIssues, scheduledChanges, otherNotifications) -> {
                        logger.info(" # Open Issues: {}", profileHealth.getOpenIssues().size());
                        logger.info(" # Schedules Changes: {}", profileHealth.getScheduledChanges().size());
                        logger.info(" # Other Notifications: {}", profileHealth.getOtherNotifications().size());
                        logger.info("");

                        profileHealth.getOpenIssues().forEach(openIssue -> logger.error(" + Open Issue: {}", openIssue));
                        profileHealth.getScheduledChanges().forEach(scheduledChange -> logger.warn(" + Scheduled Change: {}", scheduledChange));
                        profileHealth.getOtherNotifications().forEach(otherNotification -> logger.info(" + Other Notification: {}", otherNotification));

                        return null;
                    }
            );

            ProfileChecks profileChecks = getProfileChecks();

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking Trusted Advisor for profile '{}'", getProfileName());
            banner.info("=====================================================================");

            profileChecks.to(
                    ((errors, warnings, exceptions) -> {
                        logger.info(" # Errors: {}", profileChecks.getErrors().size());
                        logger.info(" # Warnings: {}", profileChecks.getWarnings().size());
                        logger.info("");

                        profileChecks.getErrors().forEach(error -> logger.error(" + Error: {}", error));
                        profileChecks.getWarnings().forEach(warning -> logger.warn(" + Warning: {}", warning));

                        return null;
                    })
            );

            ProfileSupportCases profileSupportCases = getProfileSupportCases();

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking AWS Support Cases for profile '{}'", getProfileName());
            banner.info("=====================================================================");

            profileSupportCases.to(
                    (openCases, resolvedCases) -> {
                        logger.info(" # Open Cases: {}", profileSupportCases.getOpenCases().size());
                        logger.info("");

                        profileSupportCases.getOpenCases().forEach(caseDetail -> logger.warn(" + Open Case: {}", caseDetail));

                        return null;
                    }
            );

        } catch (ExecutionException | InterruptedException e) {
            banner.info("");
            banner.info("=====================================================================");
            banner.info("An exception occurred while processing profile '{}'", getProfileName());
            banner.info("=====================================================================");

            logger.error(e);
        }
    }

}
