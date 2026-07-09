package com.bavykin.qualistrata.core.environment.matching;

/**
 * Result of matching an actual environment value against an expected baseline.
 *
 * @param matched whether the value matched.
 * @param expected expected value or pattern.
 * @param actual actual value.
 * @param message human-readable diagnostic message.
 */
public record EnvironmentValueMatchResult(
        boolean matched,
        String expected,
        String actual,
        String message
) {

    /**
     * Creates a validated match result.
     *
     * @param matched whether the value matched.
     * @param expected expected value or pattern.
     * @param actual actual value.
     * @param message human-readable diagnostic message.
     */
    public EnvironmentValueMatchResult {
        if (expected == null || expected.isBlank()) {
            throw new IllegalArgumentException("Expected environment value must not be blank.");
        }

        actual = actual == null ? "" : actual;

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Environment value match message must not be blank.");
        }
    }

    /**
     * Creates a successful match result.
     *
     * @param expected expected value or pattern.
     * @param actual actual value.
     * @return successful match result.
     */
    public static EnvironmentValueMatchResult matched(
            String expected,
            String actual
    ) {
        return new EnvironmentValueMatchResult(
                true,
                expected,
                actual,
                "Environment value matches expected baseline."
        );
    }

    /**
     * Creates a failed match result.
     *
     * @param expected expected value or pattern.
     * @param actual actual value.
     * @param message human-readable mismatch message.
     * @return failed match result.
     */
    public static EnvironmentValueMatchResult mismatched(
            String expected,
            String actual,
            String message
    ) {
        return new EnvironmentValueMatchResult(
                false,
                expected,
                actual,
                message
        );
    }
}