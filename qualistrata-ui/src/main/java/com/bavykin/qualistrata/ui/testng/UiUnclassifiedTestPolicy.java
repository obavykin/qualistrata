package com.bavykin.qualistrata.ui.testng;

import java.util.Locale;

/**
 * Runtime behaviour when a UI test has no mutation classification.
 */
public enum UiUnclassifiedTestPolicy {

    /** Allow unclassified tests but record them as unknown in the UI report. */
    ALLOW,

    /** Allow unclassified tests and print a warning to standard error. */
    WARN,

    /** Fail the test before it executes. Intended for mature CI pipelines. */
    FAIL;

    /**
     * Parses property value in a forgiving way.
     *
     * @param value raw property value.
     * @return parsed policy, defaulting to {@link #ALLOW} when blank.
     */
    public static UiUnclassifiedTestPolicy from(String value) {
        if (value == null || value.isBlank()) {
            return ALLOW;
        }

        return switch (value.trim().toLowerCase(Locale.ROOT).replace('-', '_')) {
            case "allow" -> ALLOW;
            case "warn", "warning" -> WARN;
            case "fail", "required", "require" -> FAIL;
            default -> throw new IllegalArgumentException("Unsupported UI unclassified test policy: " + value);
        };
    }
}
