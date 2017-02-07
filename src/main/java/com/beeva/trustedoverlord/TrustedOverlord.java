package com.beeva.trustedoverlord;

import com.amazonaws.services.support.model.AWSSupportException;
import com.beeva.trustedoverlord.model.ProfileChecks;
import com.beeva.trustedoverlord.service.TrustedOverlordService;
import com.beeva.trustedoverlord.service.impl.TrustedOverlordServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by cesarsilgo on 1/02/17.
 */
public class TrustedOverlord {

    private static Logger logger = LogManager.getLogger(TrustedOverlord.class);
    private static Logger banner = LogManager.getLogger("com.beeva.trustedoverlord.Banner");

    public static void main(String args[]) {

        TrustedOverlordService trustedOverlordService = new TrustedOverlordServiceImpl(args);

        int totalNumWarnings = 0;
        int totalNumErrors = 0;

        banner.info(" _____              _           _   _____                _           _");
        banner.info("|_   _|            | |         | | |  _  |              | |         | |");
        banner.info("  | |_ __ _   _ ___| |_ ___  __| | | | | |_   _____ _ __| | ___   __| |");
        banner.info("  | | '__| | | / __| __/ _ \\/ _` | | | | \\ \\ / / _ \\ '__| |/ _ \\ / _` |");
        banner.info("  | | |  | |_| \\__ \\ ||  __/ (_| | \\ \\_/ /\\ V /  __/ |  | | (_) | (_| |");
        banner.info("  \\_/_|   \\__,_|___/\\__\\___|\\__,_|  \\___/  \\_/ \\___|_|  |_|\\___/ \\__,_|");
        banner.info("");

        logger.info("");
        logger.info("...will now check {} AWS accounts. ", args.length);

        for(String profile : args) {

            banner.info("");
            banner.info("=====================================================================");
            banner.info("Checking Trusted Advisor for profile '{}'", profile);
            banner.info("=====================================================================");
            try {
                ProfileChecks profileChecks = trustedOverlordService.getProfileChecks(profile);
                logger.info(" # Errors: {}", profileChecks.getErrors().size());
                logger.info(" # Warnings: {}", profileChecks.getWarnings().size());

                logger.info("");
                for(String error : profileChecks.getErrors()) {
                    logger.error(" + Error: {}", error);
                }
                totalNumErrors += profileChecks.getErrors().size();

                for(String error : profileChecks.getWarnings()) {
                    logger.warn(" + Warning: {}", error);
                }
                totalNumWarnings += profileChecks.getWarnings().size();

            } catch (AWSSupportException ex) {
                logger.fatal("UNAUTHORIZED");
            }

        }

        logger.info("");
        logger.info("");
        logger.info("**************************************************************************");
        logger.info("TOTAL ERRORS: {}", totalNumErrors);
        logger.info("TOTAL WARNINGS: {}", totalNumWarnings);
        logger.info("**************************************************************************");


    }

}
