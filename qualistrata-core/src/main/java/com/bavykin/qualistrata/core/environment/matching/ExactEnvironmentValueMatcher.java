package com.bavykin.qualistrata.core.environment.matching;

/**
 * Matcher that validates an actual value using exact string comparison.
 */
public class ExactEnvironmentValueMatcher implements EnvironmentValueMatcher {

    private final String expected;
    private final boolean ignoreCase;

    /**
     * Creates a case-sensitive exact matcher.
     *
     * @param expected expected value.
     */
    public ExactEnvironmentValueMatcher(String expected) {
        this(expected, false);
    }

    /**
     * Creates an exact matcher.
     *
     * @param expected expected value.
     * @param ignoreCase whether comparison should ignore case.
     */
    public ExactEnvironmentValueMatcher(
            String expected,
            boolean ignoreCase
    ) {
        if (expected == null || expected.isBlank()) {
            throw new IllegalArgumentException("Expected exact environment value must not be blank.");
        }

        this.expected = expected;
        this.ignoreCase = ignoreCase;
    }

    /**
     * Matches an actual value using exact comparison.
     *
     * @param actual actual detected value.
     * @return match result.
     */
    @Override
    public EnvironmentValueMatchResult match(String actual) {
        String actualValue = actual == null ? "" : actual;

        boolean matched = ignoreCase
                ? actualValue.equalsIgnoreCase(expected)
                : actualValue.equals(expected);

        if (matched) {
            return EnvironmentValueMatchResult.matched(expected, actualValue);
        }

        return EnvironmentValueMatchResult.mismatched(
                expected,
                actualValue,
                "Unexpected environment value. Actual: "
                        + actualValue
                        + ", expected: "
                        + expected
        );
    }
}