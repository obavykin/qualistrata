package com.bavykin.qualistrata.core.environment.matching;

/**
 * Generic matcher for comparing actual environment values against expected
 * baseline values.
 *
 * <p>The matcher is intentionally independent from Unix, API, DB, UI, or any
 * delivery-project-specific logic. It can be used by any environment check
 * that needs to validate detected values such as component versions, service
 * names, command outputs, or configuration values.</p>
 */
@FunctionalInterface
public interface EnvironmentValueMatcher {

    /**
     * Matches an actual value against the matcher expectation.
     *
     * @param actual actual detected value.
     * @return match result.
     */
    EnvironmentValueMatchResult match(String actual);

    /**
     * Creates an exact value matcher.
     *
     * @param expected expected value.
     * @return exact value matcher.
     */
    static EnvironmentValueMatcher exact(String expected) {
        return new ExactEnvironmentValueMatcher(expected);
    }

    /**
     * Creates a case-insensitive exact value matcher.
     *
     * @param expected expected value.
     * @return case-insensitive exact value matcher.
     */
    static EnvironmentValueMatcher exactIgnoreCase(String expected) {
        return new ExactEnvironmentValueMatcher(expected, true);
    }

    /**
     * Creates a contains value matcher.
     *
     * @param expected expected text fragment.
     * @return contains value matcher.
     */
    static EnvironmentValueMatcher contains(String expected) {
        return new ContainsEnvironmentValueMatcher(expected);
    }

    /**
     * Creates a case-insensitive contains value matcher.
     *
     * @param expected expected text fragment.
     * @return case-insensitive contains value matcher.
     */
    static EnvironmentValueMatcher containsIgnoreCase(String expected) {
        return new ContainsEnvironmentValueMatcher(expected, true);
    }

    /**
     * Creates a regular expression value matcher.
     *
     * @param pattern regular expression pattern.
     * @return regular expression value matcher.
     */
    static EnvironmentValueMatcher regex(String pattern) {
        return new RegexEnvironmentValueMatcher(pattern);
    }
}