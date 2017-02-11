package com.beeva.trustedoverlord.overseers;

/**
 * Created by Beeva
 */
public interface Overseer{

    void shutdown();
    <T extends Overseer> T autoshutdown();

}
