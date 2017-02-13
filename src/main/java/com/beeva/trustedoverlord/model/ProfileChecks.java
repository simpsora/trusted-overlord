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

    public String toMarkdown() {

        StringBuffer result = new StringBuffer("### Trusted Advisor\n");
        errors.stream().forEach(error -> result.append("* __Error:__").append(error).append("\n"));
        warnings.stream().forEach(warning -> result.append("* __Warning:__").append(warning).append("\n"));
        exceptions.stream().forEach(exception -> result.append("* __Exception:__").append(exception).append("\n"));
        return result.toString();

    }

}
