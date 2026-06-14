package com.bavykin.qualistrata.core.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link AssertionException}.
 *
 * <p>The test verifies that assertion failures keep their message and root
 * cause. This allows framework assertions to remain diagnosable without hiding
 * the original technical reason of the failure.</p>
 */
class AssertionExceptionTest {

    @Test
    void shouldKeepMessage() {
        AssertionException exception = new AssertionException("Assertion failed");

        assertEquals("Assertion failed", exception.getMessage());
    }

    @Test
    void shouldKeepMessageAndCause() {
        RuntimeException cause = new RuntimeException("Root cause");

        AssertionException exception = new AssertionException("Assertion failed", cause);

        assertEquals("Assertion failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}