package com.beeva.trustedoverlord.reporter;

import com.amazonaws.services.health.model.AWSHealthException;
import com.amazonaws.services.support.model.AWSSupportException;
import com.beeva.trustedoverlord.model.Profile;
import com.beeva.trustedoverlord.utils.BannerLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Beeva
 */
class ReporterBuilder implements ReporterToBuilder, ReporterAndBuilder{

    private static Logger logger = LogManager.getLogger(ReporterBuilder.class);
    private static Logger banner = BannerLogger.getLogger();

    private List<String> profiles;
    private boolean toMarkdown = false;
    private boolean toLogger = false;
    private Logger loggerReporter = null;
    //Variables for reporting
    private Integer numWarnings = 0;
    private Integer numErrors = 0;
    private Integer numOpenIssues = 0;
    private Integer numSchedulesChanges = 0;
    private Integer numOtherNotifications = 0;
    private Integer numOpenCases = 0;


    ReporterBuilder(String[] profiles) {
        this.profiles = Arrays.asList(profiles);
    }


    @Override
    public ReporterBuilder and() {
        return this;
    }

    @Override
    public ReporterAndBuilder toMarkdown() {
        this.toMarkdown = true;
        return this;
    }

    @Override
    public ReporterAndBuilder toLogger() {
        return toLogger(LogManager.getLogger(Reporter.class));
    }

    @Override
    public ReporterAndBuilder toLogger(Logger logger) {
        this.toLogger = true;
        this.loggerReporter = logger;
        return this;
    }

    @Override
    public void report() {
        List<Profile> profilesModel = new LinkedList<>();
        // Initialize profiles and counters for reporting
        profiles.forEach(profileName -> {
            try {
                Profile profile = new Profile(profileName);
                profilesModel.add(profile);
                numErrors += profile.getProfileChecks().getErrors().size();
                numWarnings += profile.getProfileChecks().getWarnings().size();
                numOpenIssues += profile.getProfileHealth().getOpenIssues().size();
                numSchedulesChanges += profile.getProfileHealth().getScheduledChanges().size();
                numOtherNotifications += profile.getProfileHealth().getOtherNotifications().size();
                numOpenCases += profile.getProfileSupportCases().getOpenCases().size();
            } catch (AWSSupportException | AWSHealthException e) {
                logger.error(e);
            }
        });

        if (toLogger){
            reportToLogger(profilesModel);
        }

        if (toMarkdown){
            reportToMarkdown(profilesModel);
        }

    }

    private void reportToLogger(List<Profile> profilesModel) {
        profilesModel.forEach(profile -> {
           profile.toLogger(loggerReporter);
        });

        // Resume
        resumeReportToLogger();
    }

    private void resumeReportToLogger() {
        banner.info("");
        banner.info("");
        banner.info("**************************************************************************");
        banner.info("HEALTH:");
        banner.info("  Total Open Issues: {}", numOpenIssues);
        banner.info("  Scheduled Changes: {}", numSchedulesChanges);
        banner.info("  Other Notifications: {}", numOtherNotifications);
        banner.info("");
        banner.info("TRUSTED ADVISOR:");
        banner.info("  Total Errors: {}",numErrors);
        banner.info("  Total Warnings: {}", numWarnings);
        banner.info("");
        banner.info("SUPPORT:");
        banner.info("  Total Open Cases: {}", numOpenCases);
        banner.info("**************************************************************************");
    }

    private void reportToMarkdown(List<Profile> profilesModel) {
        StringBuffer result = new StringBuffer("# __Trusted Overlord Summary__\n")
                .append("* __Errors__: ").append(numErrors).append("\n")
                .append("* __Warnings__: ").append(numWarnings).append("\n")
                .append("* __Open Issues__: ").append(numOpenIssues).append("\n")
                .append("* __Scheduled Changes__: ").append(numSchedulesChanges).append("\n")
                .append("* __Other Notifications__: ").append(numOtherNotifications).append("\n")
                .append("\n---\n");

        profilesModel.forEach(profile -> result.append(profile.toMarkdown()));

        writeToFile(result);
    }

    private void writeToFile(StringBuffer content){
        try {
            String summaryFileName = LocalDateTime.now().format(DateTimeFormatter
                    .ofPattern("YYYY_MM_dd_hh_mm_ss")) + "_summary.md";
            Files.write(Paths.get(summaryFileName), content.toString().getBytes());
            logger.info(summaryFileName + " markdown file generated ");
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
