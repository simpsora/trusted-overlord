package com.beeva.trustedoverlord.clients;

/**
 * Created by Beeva
 */
public interface Client {

    void shutdown();
    <T extends Client> T autoshutdown();

}
