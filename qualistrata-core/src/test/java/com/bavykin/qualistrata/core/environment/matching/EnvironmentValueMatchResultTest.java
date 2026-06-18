package com.bavykin.qualistrata.core.environment.matching;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link EnvironmentValueMatchResult}.
 */
class EnvironmentValueMatchResultTest {

    @Test
    void shouldCreateMatchedResult() {
        EnvironmentValueMatchResult result = EnvironmentValueMatchResult.matched(
                "Python 3.9",
                "Python 3.9"
        );

        assertTrue(result.matched());
        assertEquals("Python 3.9", result.expected());
        assertEquals("Python 3.9", result.actual());
        assertEquals("Environment value matches expected baseline.", result.message());
    }

    @Test
    void shouldCreateMismatchedResult() {
        EnvironmentValueMatchResult result = EnvironmentValueMatchResult.mismatched(
                "Python 3.9",
                "Python 3.8",
                "Unexpected version."
        );

        assertFalse(result.matched());
        assertEquals("Python 3.9", result.expected());
        assertEquals("Python 3.8", result.actual());
        assertEquals("Unexpected version.", result.message());
    }

    @Test
    void shouldNormalizeNullActualValue() {
        EnvironmentValueMatchResult result = EnvironmentValueMatchResult.matched(
                "expected",
                null
        );

        assertEquals("", result.actual());
    }

    @Test
    void shouldFailWhenExpectedValueIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EnvironmentValueMatchResult.matched(" ", "actual")
        );

        assertEquals("Expected environment value must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenMessageIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new EnvironmentValueMatchResult(
                        true,
                        "expected",
                        "actual",
                        " "
                )
        );

        assertEquals("Environment value match message must not be blank.", exception.getMessage());
    }
}