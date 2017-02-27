package com.beeva.trustedoverlord.requirements;

import com.beeva.trustedoverlord.clients.Client;

/**
 * Created by Beeva
 */
public interface ProfileRequirement<ClientTypeToBuild extends Client> extends Requirement {

    ClientTypeToBuild clientWithProfile(String profile);

}
