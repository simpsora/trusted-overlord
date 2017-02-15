package com.beeva.trustedoverlord.model;

import com.amazonaws.services.health.model.AWSHealthException;
import com.amazonaws.services.support.model.AWSSupportException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BEEVA
 */
public class ProfileList {

    private static Logger logger = LogManager.getLogger(ProfileList.class);

    private List<Profile> profiles;
    private Integer numWarnings = 0;
    private Integer numErrors = 0;
    private Integer numOpenIssues = 0;
    private Integer numSchedulesChanges = 0;
    private Integer numOtherNotifications = 0;
    private Integer numOpenCases = 0;

    public ProfileList(List<String> profileNames) {

        profiles = new ArrayList<>(profileNames.size());
        profileNames.stream().forEach(profileName -> {
            try {
                Profile profile = new Profile(profileName);
                profiles.add(profile);
                numErrors += profile.getProfileChecks().getErrors().size();
                numWarnings += profile.getProfileChecks().getWarnings().size();
                numOpenIssues += profile.getProfileHealth().getOpenIssues().size();
                numSchedulesChanges += profile.getProfileHealth().getScheduledChanges().size();
                numOtherNotifications += profile.getProfileHealth().getOtherNotifications().size();
                numOpenCases += profile.getProfileSupportCases().getOpenCases().size();
            } catch (AWSSupportException e) {
                logger.error(e);
            } catch (AWSHealthException e) {
                logger.error(e);
            }
        });

    }

    public List<Profile> getProfiles() {
        return profiles;
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

    public String toMarkdown() {

        StringBuffer result = new StringBuffer("# __Trusted Overlord Summary__\n")
                .append("* __Errors__: ").append(numErrors).append("\n")
                .append("* __Warnings__: ").append(numWarnings).append("\n")
                .append("* __Open Issues__: ").append(numOpenIssues).append("\n")
                .append("* __Scheduled Changes__: ").append(numSchedulesChanges).append("\n")
                .append("* __Other Notifications__: ").append(numOtherNotifications).append("\n")
                .append("\n---\n");

        profiles.stream().forEach(profile -> result.append(profile.toMarkdown()));
        return result.toString();

    }

}
