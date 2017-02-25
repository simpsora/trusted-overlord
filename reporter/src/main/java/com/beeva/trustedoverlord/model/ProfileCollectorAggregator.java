package com.beeva.trustedoverlord.model;

import com.amazonaws.services.health.model.AWSHealthException;
import com.amazonaws.services.support.model.AWSSupportException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes one ProfileCollector per profile and Aggregates all the results
 * from each ProfileCollector in just one place
 *
 * Created by BEEVA
 */
public class ProfileCollectorAggregator {

    private static Logger logger = LogManager.getLogger(ProfileCollectorAggregator.class);

    private List<ProfileCollector> profileCollectors;
    private Integer numWarnings = 0;
    private Integer numErrors = 0;
    private Integer numOpenIssues = 0;
    private Integer numSchedulesChanges = 0;
    private Integer numOtherNotifications = 0;
    private Integer numOpenCases = 0;

    public ProfileCollectorAggregator(List<String> profileNames) {

        profileCollectors = new ArrayList<>(profileNames.size());
        profileNames.forEach(profileName -> {
            try {
                ProfileCollector profile = new ProfileCollector(profileName);
                profileCollectors.add(profile);
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

    }

    public List<ProfileCollector> getProfileCollectors() {
        return profileCollectors;
    }

    public Integer getNumWarnings() {
        return numWarnings;
    }

    public Integer getNumErrors() {
        return numErrors;
    }

    public Integer getNumOpenIssues() {
        return numOpenIssues;
    }

    public Integer getNumSchedulesChanges() {
        return numSchedulesChanges;
    }

    public Integer getNumOtherNotifications() {
        return numOtherNotifications;
    }

    public Integer getNumOpenCases() {
        return numOpenCases;
    }

}
