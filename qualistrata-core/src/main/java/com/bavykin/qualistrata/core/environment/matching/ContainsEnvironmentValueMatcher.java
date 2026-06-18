package com.bavykin.qualistrata.core.environment.matching;

import java.util.Locale;

/**
 * Matcher that validates whether an actual value contains an expected text
 * fragment.
 */
public class ContainsEnvironmentValueMatcher implements EnvironmentValueMatcher {

    private final String expected;
    private final boolean ignoreCase;

    /**
     * Creates a case-sensitive contains matcher.
     *
     * @param expected expected text fragment.
     */
    public ContainsEnvironmentValueMatcher(String expected) {
        this(expected, false);
    }

    /**
     * Creates a contains matcher.
     *
     * @param expected expected text fragment.
     * @param ignoreCase whether comparison should ignore case.
     */
    public ContainsEnvironmentValueMatcher(
            String expected,
            boolean ignoreCase
    ) {
        if (expected == null || expected.isBlank()) {
            throw new IllegalArgumentException("Expected contained environment value must not be blank.");
        }

        this.expected = expected;
        this.ignoreCase = ignoreCase;
    }

    /**
     * Matches an actual value by checking whether it contains the expected text.
     *
     * @param actual actual detected value.
     * @return match result.
     */
    @Override
    public EnvironmentValueMatchResult match(String actual) {
        String actualValue = actual == null ? "" : actual;

        String comparableActual = ignoreCase
                ? actualValue.toLowerCase(Locale.ROOT)
                : actualValue;

        String comparableExpected = ignoreCase
                ? expected.toLowerCase(Locale.ROOT)
                : expected;

        if (comparableActual.contains(comparableExpected)) {
            return EnvironmentValueMatchResult.matched(expected, actualValue);
        }

        return EnvironmentValueMatchResult.mismatched(
                expected,
                actualValue,
                "Environment value does not contain expected text. Actual: "
                        + actualValue
                        + ", expected to contain: "
                        + expected
        );
    }
}