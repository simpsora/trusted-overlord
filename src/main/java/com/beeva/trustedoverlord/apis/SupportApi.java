package com.beeva.trustedoverlord.apis;

import com.beeva.trustedoverlord.requirements.ProfileRequirement;
import com.beeva.trustedoverlord.clients.SupportClient;

/**
 * Created by Beeva
 */
public class SupportApi implements ProfileRequirement<SupportClient> {

    @Override
    public SupportClient clientWithProfile(String profile) {
        return new SupportClient(profile);
    }
}
