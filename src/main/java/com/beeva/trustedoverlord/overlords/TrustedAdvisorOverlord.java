package com.beeva.trustedoverlord.overlords;

import com.beeva.trustedoverlord.mutations.ProfileMutation;
import com.beeva.trustedoverlord.overseers.TrustedAdvisorOverseer;

/**
 * Created by Beeva
 */
public class TrustedAdvisorOverlord implements ProfileMutation<TrustedAdvisorOverseer> {

    @Override
    public TrustedAdvisorOverseer mutateWithProfile(String profile) {
        return new TrustedAdvisorOverseer(profile);
    }
}
