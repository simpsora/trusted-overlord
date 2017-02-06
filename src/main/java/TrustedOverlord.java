import com.amazonaws.services.support.model.AWSSupportException;
import com.amazonaws.util.StringUtils;
import model.ProfileChecks;
import service.TrustedOverlordService;
import service.impl.TrustedOverlordServiceImpl;

/**
 * Created by cesarsilgo on 1/02/17.
 */
public class TrustedOverlord {

    public static void main(String args[]) {

        TrustedOverlordService trustedOverlordService = new TrustedOverlordServiceImpl(args);

        int totalNumWarnings = 0;
        int totalNumErrors = 0;

        System.out.println(" _____              _           _   _____                _           _");
        System.out.println("|_   _|            | |         | | |  _  |              | |         | |");
        System.out.println("  | |_ __ _   _ ___| |_ ___  __| | | | | |_   _____ _ __| | ___   __| |");
        System.out.println("  | | '__| | | / __| __/ _ \\/ _` | | | | \\ \\ / / _ \\ '__| |/ _ \\ / _` |");
        System.out.println("  | | |  | |_| \\__ \\ ||  __/ (_| | \\ \\_/ /\\ V /  __/ |  | | (_) | (_| |");
        System.out.println("  \\_/_|   \\__,_|___/\\__\\___|\\__,_|  \\___/  \\_/ \\___|_|  |_|\\___/ \\__,_|");

        System.out.println("\n...will now check " + args.length + " AWS accounts. ");

        for(String profile : args) {

            System.out.println("\n=====================================================================");
            System.out.println("Checking Trusted Advisor for profile " + profile);
            System.out.println("=====================================================================\n");
            try {
                ProfileChecks profileChecks = trustedOverlordService.getProfileChecks(profile);
                System.out.println(" # Errors: " + profileChecks.getErrors().size());
                System.out.println(" # Warnings: " + profileChecks.getWarnings().size());

                System.out.println("");
                for(String error : profileChecks.getErrors()) {
                    System.out.println(" + Error: " + error);
                }
                totalNumErrors += profileChecks.getErrors().size();

                for(String error : profileChecks.getWarnings()) {
                    System.out.println(" + Warning: " + error);
                }
                totalNumWarnings += profileChecks.getWarnings().size();

            } catch (AWSSupportException ex) {
                System.out.println("UNATHORIZED\n");
            }

        }

        System.out.println("\n\n**************************************************************************");
        System.out.println("TOTAL ERRORS: "+totalNumErrors);
        System.out.println("TOTAL WARNINGS: "+totalNumWarnings);


    }

}
