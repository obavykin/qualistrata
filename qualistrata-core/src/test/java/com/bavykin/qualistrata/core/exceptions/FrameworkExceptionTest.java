package com.bavykin.qualistrata.core.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link FrameworkException}.
 *
 * <p>The test verifies that framework-level failures preserve diagnostic
 * information. This is important because higher-level modules should be able to
 * report the original failure message and root cause without losing context.</p>
 */
class FrameworkExceptionTest {

    @Test
    void shouldKeepMessage() {
        FrameworkException exception = new FrameworkException("Framework failure");

        assertEquals("Framework failure", exception.getMessage());
    }

    @Test
    void shouldKeepMessageAndCause() {
        RuntimeException cause = new RuntimeException("Root cause");

        FrameworkException exception = new FrameworkException("Framework failure", cause);

        assertEquals("Framework failure", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}