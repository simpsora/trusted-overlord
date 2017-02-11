package com.beeva.trustedoverlord.overlords;

import com.beeva.trustedoverlord.mutations.ProfileMutation;
import com.beeva.trustedoverlord.overseers.SupportOverseer;
import com.beeva.trustedoverlord.overseers.TrustedAdvisorOverseer;

/**
 * Created by Beeva
 */
public class SupportOverlord implements ProfileMutation<SupportOverseer> {

    @Override
    public SupportOverseer mutateWithProfile(String profile) {
        return new SupportOverseer(profile);
    }
}
