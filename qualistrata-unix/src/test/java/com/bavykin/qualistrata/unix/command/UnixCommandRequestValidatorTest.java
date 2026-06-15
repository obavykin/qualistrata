package com.bavykin.qualistrata.unix.command;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link UnixCommandRequestValidator}.
 *
 * <p>The tests verify generic command request validation rules that should be
 * shared by all Unix command client implementations.</p>
 */
class UnixCommandRequestValidatorTest {

    private final UnixCommandRequestValidator validator = new UnixCommandRequestValidator();

    @Test
    void shouldAcceptValidRequestWithTimeout() {
        UnixCommandRequest request = new UnixCommandRequest(
                "hostname",
                Duration.ofSeconds(10)
        );

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void shouldAcceptValidRequestWithoutTimeout() {
        UnixCommandRequest request = new UnixCommandRequest(
                "hostname",
                null
        );

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void shouldFailWhenRequestIsNull() {
        UnixCommandException exception = assertThrows(
                UnixCommandException.class,
                () -> validator.validate(null)
        );

        assertEquals("Unix command request must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenCommandIsNull() {
        UnixCommandRequest request = new UnixCommandRequest(
                null,
                Duration.ofSeconds(10)
        );

        UnixCommandException exception = assertThrows(
                UnixCommandException.class,
                () -> validator.validate(request)
        );

        assertEquals("Unix command must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenCommandIsBlank() {
        UnixCommandRequest request = new UnixCommandRequest(
                "   ",
                Duration.ofSeconds(10)
        );

        UnixCommandException exception = assertThrows(
                UnixCommandException.class,
                () -> validator.validate(request)
        );

        assertEquals("Unix command must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenTimeoutIsNegative() {
        UnixCommandRequest request = new UnixCommandRequest(
                "hostname",
                Duration.ofSeconds(-1)
        );

        UnixCommandException exception = assertThrows(
                UnixCommandException.class,
                () -> validator.validate(request)
        );

        assertEquals("Unix command timeout must not be negative.", exception.getMessage());
    }

    @Test
    void shouldFailWhenTimeoutIsZero() {
        UnixCommandRequest request = new UnixCommandRequest(
                "hostname",
                Duration.ZERO
        );

        UnixCommandException exception = assertThrows(
                UnixCommandException.class,
                () -> validator.validate(request)
        );

        assertEquals("Unix command timeout must not be zero.", exception.getMessage());
    }
}