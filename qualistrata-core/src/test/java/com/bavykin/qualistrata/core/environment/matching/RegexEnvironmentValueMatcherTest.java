package com.bavykin.qualistrata.core.environment.matching;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RegexEnvironmentValueMatcher}.
 */
class RegexEnvironmentValueMatcherTest {

    @Test
    void shouldMatchRegex() {
        EnvironmentValueMatchResult result = EnvironmentValueMatcher
                .regex("Python\\s+3\\.9\\.\\d+")
                .match("Python 3.9.18");

        assertTrue(result.matched());
    }

    @Test
    void shouldUseRegexFindInsteadOfFullMatch() {
        EnvironmentValueMatchResult result = EnvironmentValueMatcher
                .regex("3\\.9\\.\\d+")
                .match("Python 3.9.18");

        assertTrue(result.matched());
    }

    @Test
    void shouldNotMatchRegex() {
        EnvironmentValueMatchResult result = EnvironmentValueMatcher
                .regex("Python\\s+3\\.9\\.\\d+")
                .match("Python 3.8.18");

        assertFalse(result.matched());
        assertEquals(
                "Environment value does not match expected regex. Actual: Python 3.8.18, expected regex: Python\\s+3\\.9\\.\\d+",
                result.message()
        );
    }

    @Test
    void shouldFailWhenPatternIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EnvironmentValueMatcher.regex(" ")
        );

        assertEquals("Expected environment value regex pattern must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenPatternIsInvalid() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EnvironmentValueMatcher.regex("[")
        );

        assertEquals("Invalid environment value regex pattern: [", exception.getMessage());
    }
}