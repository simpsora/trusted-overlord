package com.beeva.trustedoverlord.reporter;

import com.amazonaws.services.health.model.AWSHealthException;
import com.amazonaws.services.support.model.AWSSupportException;
import com.beeva.trustedoverlord.model.ProfileCollector;
import com.beeva.trustedoverlord.model.ProfileCollectorAggregator;
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
    private ProfileCollectorAggregator pca;

    private boolean toMarkdown = false;
    private boolean toLogger = false;
    private Logger loggerReporter = null;


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
        this.pca = new ProfileCollectorAggregator(this.profileNames);

        if (toLogger){
            reportToLogger(this.pca);
        }

        if (toMarkdown){
            reportToMarkdown(this.pca);
        }

    }

    private void reportToLogger(ProfileCollectorAggregator pca) {
        pca.getProfileCollectors().forEach(
                profile -> {
                    loggerReporter.info("\n\nGenerating logger reporter for profile '{}'...", profile.getProfileName());
                    profile.toLogger(loggerReporter);
                }
        );

        // Resume
        resumeReportToLogger(pca);
    }

    private void resumeReportToLogger(ProfileCollectorAggregator pca) {
        banner.info("");
        banner.info("");
        banner.info("**************************************************************************");
        banner.info("HEALTH:");
        banner.info("  Total Open Issues: {}", pca.getNumOpenIssues());
        banner.info("  Scheduled Changes: {}", pca.getNumSchedulesChanges());
        banner.info("  Other Notifications: {}", pca.getNumOtherNotifications());
        banner.info("");
        banner.info("TRUSTED ADVISOR:");
        banner.info("  Total Errors: {}",pca.getNumErrors());
        banner.info("  Total Warnings: {}", pca.getNumWarnings());
        banner.info("");
        banner.info("SUPPORT:");
        banner.info("  Total Open Cases: {}", pca.getNumOpenCases());
        banner.info("**************************************************************************");
        banner.info("");
    }

    private void reportToMarkdown(ProfileCollectorAggregator pca) {
        StringBuffer result = new StringBuffer("# __Trusted Overlord Summary__\n")
                .append("#### HEALTH").append("\n")
                .append("* __Open Issues__: ").append(pca.getNumOpenIssues()).append("\n")
                .append("* __Scheduled Changes__: ").append(pca.getNumSchedulesChanges()).append("\n")
                .append("* __Other Notifications__: ").append(pca.getNumOtherNotifications()).append("\n")
                .append("#### TRUSTED ADVISOR").append("\n")
                .append("* __Errors__: ").append(pca.getNumErrors()).append("\n")
                .append("* __Warnings__: ").append(pca.getNumWarnings()).append("\n")
                .append("#### SUPPORT").append("\n")
                .append("* __Open Cases__: ").append(pca.getNumOpenCases()).append("\n")
                .append("\n---\n");

        pca.getProfileCollectors().forEach(profile -> result.append(profile.toMarkdown()));

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
