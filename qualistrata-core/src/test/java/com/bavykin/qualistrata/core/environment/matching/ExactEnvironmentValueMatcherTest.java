package com.bavykin.qualistrata.core.environment.matching;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ExactEnvironmentValueMatcher}.
 */
class ExactEnvironmentValueMatcherTest {

    @Test
    void shouldMatchExactValue() {
        EnvironmentValueMatchResult result = EnvironmentValueMatcher
                .exact("Python 3.9")
                .match("Python 3.9");

        assertTrue(result.matched());
    }

    @Test
    void shouldNotMatchDifferentValue() {
        EnvironmentValueMatchResult result = EnvironmentValueMatcher
                .exact("Python 3.9")
                .match("Python 3.8");

        assertFalse(result.matched());
        assertEquals(
                "Unexpected environment value. Actual: Python 3.8, expected: Python 3.9",
                result.message()
        );
    }

    @Test
    void shouldMatchExactValueIgnoringCase() {
        EnvironmentValueMatchResult result = EnvironmentValueMatcher
                .exactIgnoreCase("rhel")
                .match("RHEL");

        assertTrue(result.matched());
    }

    @Test
    void shouldFailWhenExpectedValueIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EnvironmentValueMatcher.exact(" ")
        );

        assertEquals("Expected exact environment value must not be blank.", exception.getMessage());
    }
}