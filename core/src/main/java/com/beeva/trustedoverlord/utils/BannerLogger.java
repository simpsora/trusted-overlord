package com.beeva.trustedoverlord.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by BEEVA
 */
public class BannerLogger {

    private BannerLogger() {}

    public static Logger getLogger(){
        return LogManager.getLogger("com.beeva.trustedoverlord.utils.BannerLogger");
    }

}
