package com.beeva.trustedoverlord;

import com.amazonaws.services.health.model.AWSHealthException;
import com.amazonaws.services.support.model.AWSSupportException;
import com.beeva.trustedoverlord.model.ProfileChecks;
import com.beeva.trustedoverlord.model.ProfileHealth;
import com.beeva.trustedoverlord.model.ProfileSupportCases;
import com.beeva.trustedoverlord.utils.BannerLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

/**
 *
 * Created by cesarsilgo on 1/02/17.
 */
public class TrustedOverlordMain {

    private static Logger logger = LogManager.getLogger(TrustedOverlordMain.class);
    private static Logger banner = BannerLogger.getLogger();

    public static void main(String args[]) {

        int totalNumWarnings = 0;
        int totalNumErrors = 0;
        int totalNumOpenIssues = 0;
        int totalNumSchedulesChanges = 0;
        int totalNumOtherNotifications = 0;
        int totalOpenCases = 0;

        banner.info(" _____              _           _   _____                _               _");
        banner.info("|_   _|            | |         | | |  _  |              | |             | |");
        banner.info("  | |_ __ _   _ ___| |_ ___  __| | | | | |_   _____ _ __| | ___  _ __ __| |");
        banner.info("  | | '__| | | / __| __/ _ \\/ _` | | | | \\ \\ / / _ \\ '__| |/ _ \\| '__/ _` |");
        banner.info("  | | |  | |_| \\__ \\ ||  __/ (_| | \\ \\_/ /\\ V /  __/ |  | | (_) | |  |(_| |");
        banner.info("  \\_/_|   \\__,_|___/\\__\\___|\\__,_|  \\___/  \\_/ \\___|_|  |_|\\___/|_|  \\__,_|");
        banner.info("");

        banner.info("");
        logger.info("...will now check {} AWS accounts. ", args.length);

        for(String profile : args) {

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking Health for profile '{}'", profile);
            banner.info("=====================================================================");

            try {
                ProfileHealth profileHealth =
                        TrustedOverlordClientFactory.healthApi()
                            .clientWithProfile(profile)
                                .autoshutdown()
                                .getProfileHealth()
                                .get();

                logger.info(" # Open Issues: {}", profileHealth.getOpenIssues().size());
                logger.info(" # Schedules Changes: {}", profileHealth.getScheduledChanges().size());
                logger.info(" # Other Notifications: {}", profileHealth.getOtherNotifications().size());
                logger.info("");

                for(String openIssue : profileHealth.getOpenIssues()) {
                    logger.error(" + Open Issue: {}", openIssue);
                }
                totalNumOpenIssues += profileHealth.getOpenIssues().size();

                for(String scheduledChange : profileHealth.getScheduledChanges()) {
                    logger.warn(" + Scheduled Change: {}", scheduledChange);
                }
                totalNumSchedulesChanges += profileHealth.getScheduledChanges().size();

                for(String otherNotification : profileHealth.getOtherNotifications()) {
                    logger.info(" + Other Notification: {}", otherNotification);
                }
                totalNumOtherNotifications += profileHealth.getOtherNotifications().size();

            } catch (AWSHealthException ex) {
                logger.error("UNAUTHORIZED AWS Health", ex);
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e);
            }

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking Trusted Advisor for profile '{}'", profile);
            banner.info("=====================================================================");
            try {
                ProfileChecks profileChecks =
                        TrustedOverlordClientFactory.trustedAdvisorApi()
                            .clientWithProfile(profile)
                                .autoshutdown()
                                .getProfileChecks()
                                .get();

                logger.info(" # Errors: {}", profileChecks.getErrors().size());
                logger.info(" # Warnings: {}", profileChecks.getWarnings().size());
                logger.info("");

                for(String error : profileChecks.getErrors()) {
                    logger.error(" + Error: {}", error);
                }
                totalNumErrors += profileChecks.getErrors().size();

                for(String error : profileChecks.getWarnings()) {
                    logger.warn(" + Warning: {}", error);
                }
                totalNumWarnings += profileChecks.getWarnings().size();

            } catch (AWSSupportException ex) {
                logger.error("UNAUTHORIZED AWS Trusted Advisor", ex);
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e);
            }

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking AWS Support Cases for profile '{}'", profile);
            banner.info("=====================================================================");

            try {
                ProfileSupportCases profileSupportCases =
                        TrustedOverlordClientFactory.supportApi()
                            .clientWithProfile(profile)
                                .autoshutdown()
                                .getSupportCases()
                                .get();
                logger.info(" # Open Cases: {}", profileSupportCases.getOpenCases().size());
                logger.info("");

                for (ProfileSupportCases.Case caseDetail : profileSupportCases.getOpenCases()){
                    logger.warn(" + Open Case: {}", caseDetail);
                }
                totalOpenCases += profileSupportCases.getOpenCases().size();

            } catch (AWSSupportException ex) {
                logger.error("UNAUTHORIZED AWS Support", ex);
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e);
            }

        }

        banner.info("");
        banner.info("");
        banner.info("**************************************************************************");
        banner.info("HEALTH:");
        banner.info("  Total Open Issues: {}", totalNumOpenIssues);
        banner.info("  Scheduled Changes: {}", totalNumSchedulesChanges);
        banner.info("  Other Notifications: {}", totalNumOtherNotifications);
        banner.info("");
        banner.info("TRUSTED ADVISOR:");
        banner.info("  Total Errors: {}", totalNumErrors);
        banner.info("  Total Warnings: {}", totalNumWarnings);
        banner.info("");
        banner.info("SUPPORT:");
        banner.info("  Total Open Cases: {}", totalOpenCases);
        banner.info("**************************************************************************");

    }

}
