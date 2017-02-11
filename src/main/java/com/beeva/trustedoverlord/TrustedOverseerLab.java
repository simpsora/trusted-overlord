package com.beeva.trustedoverlord;

import com.beeva.trustedoverlord.overlords.HealthOverlord;
import com.beeva.trustedoverlord.overlords.SupportOverlord;
import com.beeva.trustedoverlord.overlords.TrustedAdvisorOverlord;

/**
 * First class for the Fluent API
 */
public class TrustedOverseerLab {

    public static TrustedAdvisorOverlord trustedAdvisorOverlord(){
        return new TrustedAdvisorOverlord();
    }

    public static SupportOverlord supportOverlord(){
        return new SupportOverlord();
    }
    public static HealthOverlord healthOverlord(){
        return new HealthOverlord();
    }

}
