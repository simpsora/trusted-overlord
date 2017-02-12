package com.beeva.trustedoverlord.apis;

import com.beeva.trustedoverlord.requirements.ProfileRequirement;
import com.beeva.trustedoverlord.clients.HealthClient;

/**
 * Created by Beeva
 */
public class HealthApi implements ProfileRequirement<HealthClient> {

    @Override
    public HealthClient clientWithProfile(String profile) {
        return new HealthClient(profile);
    }
}
