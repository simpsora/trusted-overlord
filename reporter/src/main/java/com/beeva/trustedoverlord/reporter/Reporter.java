package com.beeva.trustedoverlord.reporter;

/**
 * Created by Beeva
 */
public interface Reporter {

    static ReporterToBuilder fromProfiles(String... profiles){
        return new ReporterBuilder(profiles);
    }

}
