package com.beeva.trustedoverlord.service;

import com.beeva.trustedoverlord.model.ProfileChecks;
import com.beeva.trustedoverlord.model.ProfileHealth;

/**
 * Created by cesarsilgo on 31/01/17.
 */
public interface TrustedOverlordService {

    ProfileChecks getProfileChecks(String profile);

    ProfileHealth getProfileHealth(String profile);

}
