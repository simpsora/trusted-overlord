package com.beeva.trustedoverlord.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model class to store the results of the invocation to the AWS TrustedAdvisor API
 */
public class ProfileChecks {

    private List<String> errors;
    private List<String> warnings;
    private List<Exception> exceptions;

    public ProfileChecks() {
        this.errors = Collections.synchronizedList(new ArrayList<>());
        this.warnings = Collections.synchronizedList(new ArrayList<>());
        this.exceptions = Collections.synchronizedList(new ArrayList<>());
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public void addException(Exception exception) {
        this.exceptions.add(exception);
    }

    public <T> T to(ExportTo<T> func){
        return func.export(getErrors(), getWarnings(), getExceptions());
    }

    @FunctionalInterface
    interface ExportTo<T> {
        T export(List<String> errors, List<String> warnings, List<Exception> exceptions);
    }
}
