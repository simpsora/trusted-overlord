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
    private ProfileChecks profileChecks;
    private ProfileHealth profileHealth;
    private ProfileSupportCases profileSupportCases;

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

}
