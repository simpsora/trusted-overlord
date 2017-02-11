package com.beeva.trustedoverlord.mutations;

import com.beeva.trustedoverlord.overseers.Overseer;

/**
 * Created by Beeva
 */
public interface ProfileMutation<T extends Overseer>  extends Mutation {

    T mutateWithProfile(String profile);

}
