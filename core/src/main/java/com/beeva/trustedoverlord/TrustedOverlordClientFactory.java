package com.beeva.trustedoverlord;

import com.beeva.trustedoverlord.apis.HealthApi;
import com.beeva.trustedoverlord.apis.SupportApi;
import com.beeva.trustedoverlord.apis.TrustedAdvisorApi;

/**
 * First class for the Fluent API
 */
public class TrustedOverlordClientFactory {

    public static TrustedAdvisorApi trustedAdvisorApi(){
        return new TrustedAdvisorApi();
    }

    public static SupportApi supportApi(){
        return new SupportApi();
    }

    public static HealthApi healthApi(){
        return new HealthApi();
    }

}
