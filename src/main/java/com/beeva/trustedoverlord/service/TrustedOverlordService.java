package com.beeva.trustedoverlord.service;

import com.beeva.trustedoverlord.model.ProfileChecks;
import com.beeva.trustedoverlord.model.ProfileHealth;
import com.beeva.trustedoverlord.model.ProfileSupportCases;

import java.util.concurrent.Future;

/**
 * Created by cesarsilgo on 31/01/17.
 */
public interface TrustedOverlordService {

    Future<ProfileChecks> getProfileChecks(String profile);

    Future<ProfileHealth> getProfileHealth(String profile);

    Future<ProfileSupportCases> getSupportCases(String profile);

    void shutdown();
}
