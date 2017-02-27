package com.beeva.trustedoverlord;

import com.beeva.trustedoverlord.reporter.Reporter;
import com.beeva.trustedoverlord.utils.BannerLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by BEEVA
 */
public class TrustedOverlordMain {

    private static Logger logger = LogManager.getLogger(TrustedOverlordMain.class);
    private static Logger banner = BannerLogger.getLogger();

    public static void main(String args[]) {

        if (args.length < 1) {
            logger.error("Invalid number of arguments please provide at least one AWS profile name");
            return;
        }


        banner.info(" _____              _           _   _____                _               _");
        banner.info("|_   _|            | |         | | |  _  |              | |             | |");
        banner.info("  | |_ __ _   _ ___| |_ ___  __| | | | | |_   _____ _ __| | ___  _ __ __| |");
        banner.info("  | | '__| | | / __| __/ _ \\/ _` | | | | \\ \\ / / _ \\ '__| |/ _ \\| '__/ _` |");
        banner.info("  | | |  | |_| \\__ \\ ||  __/ (_| | \\ \\_/ /\\ V /  __/ |  | | (_) | |  |(_| |");
        banner.info("  \\_/_|   \\__,_|___/\\__\\___|\\__,_|  \\___/  \\_/ \\___|_|  |_|\\___/|_|  \\__,_|");
        banner.info("");
        banner.info("");
        logger.info("...will now check {} AWS accounts. ", args.length);


        Reporter.fromProfiles(args).toMarkdown().and().toLogger(logger).report();

    }

}
