package com.splicelab.data.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DataValidationReport {
    public enum Severity { INFO, WARNING, ERROR }

    public record Issue(Severity severity, String source, String message) {
    }

    private final List<Issue> issues = new ArrayList<>();

    public void info(String source, String message) {
        add(Severity.INFO, source, message);
    }

    public void warn(String source, String message) {
        add(Severity.WARNING, source, message);
    }

    public void error(String source, String message) {
        add(Severity.ERROR, source, message);
    }

    public void add(Severity severity, String source, String message) {
        issues.add(new Issue(severity, safe(source), safe(message)));
    }

    public boolean hasErrors() {
        for (Issue i : issues) {
            if (i.severity == Severity.ERROR) return true;
        }
        return false;
    }

    public List<Issue> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}

