package com.beeva.trustedoverlord.service;

import com.beeva.trustedoverlord.model.ProfileChecks;
import com.beeva.trustedoverlord.model.ProfileHealth;

import java.util.concurrent.Future;

/**
 * Created by cesarsilgo on 31/01/17.
 */
public interface TrustedOverlordService {

    Future<ProfileChecks> getProfileChecks(String profile);

    ProfileHealth getProfileHealth(String profile);

    void shutdown();
}
