package com.beeva.trustedoverlord;

import com.beeva.trustedoverlord.model.Profile;
import com.beeva.trustedoverlord.model.ProfileList;
import com.beeva.trustedoverlord.model.ProfileSupportCases;
import com.beeva.trustedoverlord.utils.BannerLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Created by BEEVA
 */
public class TrustedOverlordMain {

    private static Logger logger = LogManager.getLogger(TrustedOverlordMain.class);
    private static Logger banner = BannerLogger.getLogger();

    public static void main(String args[]) {

        if (args.length < 1) {
            logger.error("Invalid number of arguments please provide at least one AWS profile name");
            return;
        }


        banner.info(" _____              _           _   _____                _               _");
        banner.info("|_   _|            | |         | | |  _  |              | |             | |");
        banner.info("  | |_ __ _   _ ___| |_ ___  __| | | | | |_   _____ _ __| | ___  _ __ __| |");
        banner.info("  | | '__| | | / __| __/ _ \\/ _` | | | | \\ \\ / / _ \\ '__| |/ _ \\| '__/ _` |");
        banner.info("  | | |  | |_| \\__ \\ ||  __/ (_| | \\ \\_/ /\\ V /  __/ |  | | (_) | |  |(_| |");
        banner.info("  \\_/_|   \\__,_|___/\\__\\___|\\__,_|  \\___/  \\_/ \\___|_|  |_|\\___/|_|  \\__,_|");
        banner.info("");
        banner.info("");
        logger.info("...will now check {} AWS accounts. ", args.length);

        ProfileList profileList = new ProfileList(Arrays.asList(args));

        for (Profile profile : profileList.getProfiles()) {

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking Health for profile '{}'", profile.getProfileName());
            banner.info("=====================================================================");

            if (profile.getProfileHealth() != null) {
                logger.info(" # Open Issues: {}", profile.getProfileHealth().getOpenIssues().size());
                logger.info(" # Schedules Changes: {}", profile.getProfileHealth().getScheduledChanges().size());
                logger.info(" # Other Notifications: {}", profile.getProfileHealth().getOtherNotifications().size());
                logger.info("");

                for (String openIssue : profile.getProfileHealth().getOpenIssues()) {
                    logger.error(" + Open Issue: {}", openIssue);
                }
                for (String scheduledChange : profile.getProfileHealth().getScheduledChanges()) {
                    logger.warn(" + Scheduled Change: {}", scheduledChange);
                }
                for (String otherNotification : profile.getProfileHealth().getOtherNotifications()) {
                    logger.info(" + Other Notification: {}", otherNotification);
                }
            }

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking Trusted Advisor for profile '{}'", profile.getProfileName());
            banner.info("=====================================================================");
            if (profile.getProfileChecks() != null) {

                logger.info(" # Errors: {}", profile.getProfileChecks().getErrors().size());
                logger.info(" # Warnings: {}", profile.getProfileChecks().getWarnings().size());
                logger.info("");

                for (String error : profile.getProfileChecks().getErrors()) {
                    logger.error(" + Error: {}", error);
                }

                for (String error : profile.getProfileChecks().getWarnings()) {
                    logger.warn(" + Warning: {}", error);
                }
            }

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking AWS Support Cases for profile '{}'", profile.getProfileName());
            banner.info("=====================================================================");

            if (profile.getProfileSupportCases() != null) {

                logger.info(" # Open Cases: {}", profile.getProfileSupportCases().getOpenCases().size());
                logger.info("");

                for (ProfileSupportCases.Case caseDetail : profile.getProfileSupportCases().getOpenCases()) {
                    logger.warn(" + Open Case: {}", caseDetail);
                }
            }

        }

        banner.info("");
        banner.info("");
        banner.info("**************************************************************************");
        banner.info("HEALTH:");
        banner.info("  Total Open Issues: {}", profileList.getNumOpenIssues());
        banner.info("  Scheduled Changes: {}", profileList.getNumSchedulesChanges());
        banner.info("  Other Notifications: {}", profileList.getNumOtherNotifications());
        banner.info("");
        banner.info("TRUSTED ADVISOR:");
        banner.info("  Total Errors: {}", profileList.getNumErrors());
        banner.info("  Total Warnings: {}", profileList.getNumWarnings());
        banner.info("");
        banner.info("SUPPORT:");
        banner.info("  Total Open Cases: {}", profileList.getNumOpenCases());
        banner.info("**************************************************************************");

        try {
            String summaryFileName = LocalDateTime.now().format(DateTimeFormatter
                    .ofPattern("YYYY_MM_dd_hh_mm_ss")) + "_summary.md";
            Files.write(Paths.get(summaryFileName), profileList.toMarkdown().getBytes());
            logger.info(summaryFileName + " markdown file generated ");
        } catch (IOException e) {
            logger.error(e);
        }

    }

}
