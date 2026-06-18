package com.bavykin.qualistrata.core.environment.matching;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Matcher that validates an actual value using a regular expression.
 */
public class RegexEnvironmentValueMatcher implements EnvironmentValueMatcher {

    private final String pattern;
    private final Pattern compiledPattern;

    /**
     * Creates a regular expression matcher.
     *
     * @param pattern regular expression pattern.
     */
    public RegexEnvironmentValueMatcher(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalArgumentException("Expected environment value regex pattern must not be blank.");
        }

        try {
            this.compiledPattern = Pattern.compile(pattern);
        } catch (PatternSyntaxException exception) {
            throw new IllegalArgumentException(
                    "Invalid environment value regex pattern: " + pattern,
                    exception
            );
        }

        this.pattern = pattern;
    }

    /**
     * Matches an actual value using regular expression search.
     *
     * <p>The matcher uses {@link java.util.regex.Matcher#find()} instead of
     * {@code matches()} so consumers can validate command outputs that contain
     * additional text around the expected value.</p>
     *
     * @param actual actual detected value.
     * @return match result.
     */
    @Override
    public EnvironmentValueMatchResult match(String actual) {
        String actualValue = actual == null ? "" : actual;

        if (compiledPattern.matcher(actualValue).find()) {
            return EnvironmentValueMatchResult.matched(pattern, actualValue);
        }

        return EnvironmentValueMatchResult.mismatched(
                pattern,
                actualValue,
                "Environment value does not match expected regex. Actual: "
                        + actualValue
                        + ", expected regex: "
                        + pattern
        );
    }
}