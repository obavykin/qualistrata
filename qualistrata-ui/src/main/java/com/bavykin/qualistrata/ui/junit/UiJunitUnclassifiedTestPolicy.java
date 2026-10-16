package com.bavykin.qualistrata.ui.junit;

/** Behaviour for UI tests that have no mutation classification. */
public enum UiJunitUnclassifiedTestPolicy {
    /** Keep migration safe and allow unclassified tests. */
    ALLOW,

    /** Print a warning through the thrown validation message only when strict tooling inspects reports later. */
    WARN,

    /** Fail fast when a UI test is not classified. */
    FAIL;

    /**
     * Parses policy from runtime property value.
     *
     * @param value value to enter or select.
     * @return the resulting value.
     */
    public static UiJunitUnclassifiedTestPolicy from(String value) {
        if (value == null || value.isBlank()) {
            return ALLOW;
        }
        return switch (value.trim().toLowerCase()) {
            case "warn" -> WARN;
            case "fail" -> FAIL;
            case "allow" -> ALLOW;
            default -> throw new IllegalArgumentException(
                    "Unsupported UI unclassified test policy: " + value + ". Supported values: allow, warn, fail."
            );
        };
    }
}
