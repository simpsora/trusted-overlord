package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cesarsilgo on 1/02/17.
 */
public class ProfileChecks {

    private String profile;
    private List<String> errors;
    private List<String> warnings;

    public ProfileChecks(String profile) {
        this.profile = profile;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

}
