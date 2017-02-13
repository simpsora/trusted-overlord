package com.beeva.trustedoverlord;

import com.amazonaws.services.health.model.AWSHealthException;
import com.amazonaws.services.support.model.AWSSupportException;
import com.beeva.trustedoverlord.model.Profile;
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

        for(String profileName : args) {

            Profile profile = new Profile(profileName);

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking Health for profile '{}'", profile.getProfileName());
            banner.info("=====================================================================");
            if(profile.getProfileHealth()!=null) {

                logger.info(" # Open Issues: {}", profile.getProfileHealth().getOpenIssues().size());
                logger.info(" # Schedules Changes: {}", profile.getProfileHealth().getScheduledChanges().size());
                logger.info(" # Other Notifications: {}", profile.getProfileHealth().getOtherNotifications().size());
                logger.info("");

                for (String openIssue : profile.getProfileHealth().getOpenIssues()) {
                    logger.error(" + Open Issue: {}", openIssue);
                }
                totalNumOpenIssues += profile.getProfileHealth().getOpenIssues().size();

                for (String scheduledChange : profile.getProfileHealth().getScheduledChanges()) {
                    logger.warn(" + Scheduled Change: {}", scheduledChange);
                }
                totalNumSchedulesChanges += profile.getProfileHealth().getScheduledChanges().size();

                for (String otherNotification : profile.getProfileHealth().getOtherNotifications()) {
                    logger.info(" + Other Notification: {}", otherNotification);
                }
                totalNumOtherNotifications += profile.getProfileHealth().getOtherNotifications().size();
            }

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking Trusted Advisor for profile '{}'", profile.getProfileName());
            banner.info("=====================================================================");
            if(profile.getProfileChecks()!=null) {

                logger.info(" # Errors: {}", profile.getProfileChecks().getErrors().size());
                logger.info(" # Warnings: {}", profile.getProfileChecks().getWarnings().size());
                logger.info("");

                for (String error : profile.getProfileChecks().getErrors()) {
                    logger.error(" + Error: {}", error);
                }
                totalNumErrors += profile.getProfileChecks().getErrors().size();

                for (String error : profile.getProfileChecks().getWarnings()) {
                    logger.warn(" + Warning: {}", error);
                }
                totalNumWarnings += profile.getProfileChecks().getWarnings().size();
            }

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking AWS Support Cases for profile '{}'", profile.getProfileName());
            banner.info("=====================================================================");

            if(profile.getProfileSupportCases()!=null) {

                logger.info(" # Open Cases: {}", profile.getProfileSupportCases().getOpenCases().size());
                logger.info("");

                for (ProfileSupportCases.Case caseDetail : profile.getProfileSupportCases().getOpenCases()) {
                    logger.warn(" + Open Case: {}", caseDetail);
                }
                totalOpenCases += profile.getProfileSupportCases().getOpenCases().size();
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
