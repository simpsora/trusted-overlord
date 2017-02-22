package com.beeva.trustedoverlord.reporter;

import com.amazonaws.services.health.model.AWSHealthException;
import com.amazonaws.services.support.model.AWSSupportException;
import com.beeva.trustedoverlord.model.ProfileCollector;
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

    private List<String> profileNames;
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


    ReporterBuilder(String[] profileNames) {
        this.profileNames = Arrays.asList(profileNames);
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
        List<ProfileCollector> profileCollectors = new LinkedList<>();
        // Initialize profiles and counters for reporting
        profileNames.forEach(profileName -> {
            try {
                ProfileCollector profileCollector = new ProfileCollector(profileName);
                profileCollectors.add(profileCollector);
                numErrors += profileCollector.getProfileChecks().getErrors().size();
                numWarnings += profileCollector.getProfileChecks().getWarnings().size();
                numOpenIssues += profileCollector.getProfileHealth().getOpenIssues().size();
                numSchedulesChanges += profileCollector.getProfileHealth().getScheduledChanges().size();
                numOtherNotifications += profileCollector.getProfileHealth().getOtherNotifications().size();
                numOpenCases += profileCollector.getProfileSupportCases().getOpenCases().size();
            } catch (AWSSupportException | AWSHealthException e) {
                logger.error(e);
            }
        });

        if (toLogger){
            reportToLogger(profileCollectors);
        }

        if (toMarkdown){
            reportToMarkdown(profileCollectors);
        }

    }

    private void reportToLogger(List<ProfileCollector> profilesModel) {
        profilesModel.forEach(
                profile -> profile.toLogger(loggerReporter)
        );

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

    private void reportToMarkdown(List<ProfileCollector> profilesModel) {
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
