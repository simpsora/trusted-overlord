package com.beeva.trustedoverlord.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lists of errors and warnings
 */
public class ProfileChecks {

    private List<String> errors;
    private List<String> warnings;

    public ProfileChecks() {
        this.errors = Collections.synchronizedList(new ArrayList<>());
        this.warnings = Collections.synchronizedList(new ArrayList<>());
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
