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

    public static void main(String args[]) {

        TrustedOverlordService trustedOverlordService = new TrustedOverlordServiceImpl(args);

        int totalNumWarnings = 0;
        int totalNumErrors = 0;

        logger.info(" _____              _           _   _____                _           _");
        logger.info("|_   _|            | |         | | |  _  |              | |         | |");
        logger.info("  | |_ __ _   _ ___| |_ ___  __| | | | | |_   _____ _ __| | ___   __| |");
        logger.info("  | | '__| | | / __| __/ _ \\/ _` | | | | \\ \\ / / _ \\ '__| |/ _ \\ / _` |");
        logger.info("  | | |  | |_| \\__ \\ ||  __/ (_| | \\ \\_/ /\\ V /  __/ |  | | (_) | (_| |");
        logger.info("  \\_/_|   \\__,_|___/\\__\\___|\\__,_|  \\___/  \\_/ \\___|_|  |_|\\___/ \\__,_|");

        logger.info("\n");
        logger.info("...will now check " + args.length + " AWS accounts. ");

        for(String profile : args) {

            logger.info("=====================================================================");
            logger.info("Checking Trusted Advisor for profile " + profile);
            logger.info("=====================================================================");
            try {
                ProfileChecks profileChecks = trustedOverlordService.getProfileChecks(profile);
                logger.info(" # Errors: " + profileChecks.getErrors().size());
                logger.info(" # Warnings: " + profileChecks.getWarnings().size());

                logger.info("");
                for(String error : profileChecks.getErrors()) {
                    logger.error(" + Error: " + error);
                }
                totalNumErrors += profileChecks.getErrors().size();

                for(String error : profileChecks.getWarnings()) {
                    logger.warn(" + Warning: " + error);
                }
                totalNumWarnings += profileChecks.getWarnings().size();

            } catch (AWSSupportException ex) {
                logger.fatal("UNAUTHORIZED");
            }

        }

        logger.info("\n\n");
        logger.info("**************************************************************************");
        logger.info("TOTAL ERRORS: "+totalNumErrors);
        logger.info("TOTAL WARNINGS: "+totalNumWarnings);


    }

}
