package com.bavykin.qualistrata.core.environment.matching;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ContainsEnvironmentValueMatcher}.
 */
class ContainsEnvironmentValueMatcherTest {

    @Test
    void shouldMatchContainedText() {
        EnvironmentValueMatchResult result = EnvironmentValueMatcher
                .contains("Python 3.9")
                .match("Python 3.9.18");

        assertTrue(result.matched());
    }

    @Test
    void shouldNotMatchMissingText() {
        EnvironmentValueMatchResult result = EnvironmentValueMatcher
                .contains("Python 3.9")
                .match("Python 3.8.18");

        assertFalse(result.matched());
        assertEquals(
                "Environment value does not contain expected text. Actual: Python 3.8.18, expected to contain: Python 3.9",
                result.message()
        );
    }

    @Test
    void shouldMatchContainedTextIgnoringCase() {
        EnvironmentValueMatchResult result = EnvironmentValueMatcher
                .containsIgnoreCase("apache")
                .match("Apache HTTP Server");

        assertTrue(result.matched());
    }

    @Test
    void shouldFailWhenExpectedValueIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EnvironmentValueMatcher.contains(" ")
        );

        assertEquals("Expected contained environment value must not be blank.", exception.getMessage());
    }
}