package com.beeva.trustedoverlord.overlords;

import com.beeva.trustedoverlord.mutations.ProfileMutation;
import com.beeva.trustedoverlord.overseers.HealthOverseer;
import com.beeva.trustedoverlord.overseers.SupportOverseer;

/**
 * Created by Beeva
 */
public class HealthOverlord implements ProfileMutation<HealthOverseer> {

    @Override
    public HealthOverseer mutateWithProfile(String profile) {
        return new HealthOverseer(profile);
    }
}
