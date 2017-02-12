package com.beeva.trustedoverlord.apis;

import com.beeva.trustedoverlord.requirements.ProfileRequirement;
import com.beeva.trustedoverlord.clients.TrustedAdvisorClient;

/**
 * Created by Beeva
 */
public class TrustedAdvisorApi implements ProfileRequirement<TrustedAdvisorClient> {

    @Override
    public TrustedAdvisorClient clientWithProfile(String profile) {
        return new TrustedAdvisorClient(profile);
    }
}
