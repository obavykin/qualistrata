package com.bavykin.qualistrata.unix.ssh;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link SshConnectionRequestValidator}.
 *
 * <p>The tests verify generic SSH connection request validation before any
 * concrete SSH library implementation is introduced.</p>
 */
class SshConnectionRequestValidatorTest {

    private final SshConnectionRequestValidator validator = new SshConnectionRequestValidator();

    @Test
    void shouldAcceptValidPasswordRequest() {
        SshConnectionRequest request = new SshConnectionRequest(
                "app.demo.test",
                22,
                "app",
                "password",
                null,
                Duration.ofSeconds(10)
        );

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void shouldAcceptValidKeyRequest() {
        SshConnectionRequest request = new SshConnectionRequest(
                "app.demo.test",
                22,
                "app",
                null,
                "/home/app/.ssh/id_rsa",
                Duration.ofSeconds(10)
        );

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void shouldAcceptRequestWithoutPortAndTimeout() {
        SshConnectionRequest request = new SshConnectionRequest(
                "app.demo.test",
                null,
                "app",
                "password",
                null,
                null
        );

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void shouldFailWhenRequestIsNull() {
        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> validator.validate(null)
        );

        assertEquals("SSH connection request must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostIsBlank() {
        SshConnectionRequest request = new SshConnectionRequest(
                " ",
                22,
                "app",
                "password",
                null,
                Duration.ofSeconds(10)
        );

        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> validator.validate(request)
        );

        assertEquals("SSH host must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenPortIsNotPositive() {
        SshConnectionRequest request = new SshConnectionRequest(
                "app.demo.test",
                0,
                "app",
                "password",
                null,
                Duration.ofSeconds(10)
        );

        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> validator.validate(request)
        );

        assertEquals("SSH port must be positive when configured.", exception.getMessage());
    }

    @Test
    void shouldFailWhenUsernameIsBlank() {
        SshConnectionRequest request = new SshConnectionRequest(
                "app.demo.test",
                22,
                " ",
                "password",
                null,
                Duration.ofSeconds(10)
        );

        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> validator.validate(request)
        );

        assertEquals("SSH username must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenTimeoutIsNegative() {
        SshConnectionRequest request = new SshConnectionRequest(
                "app.demo.test",
                22,
                "app",
                "password",
                null,
                Duration.ofSeconds(-1)
        );

        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> validator.validate(request)
        );

        assertEquals("SSH connection timeout must not be negative.", exception.getMessage());
    }

    @Test
    void shouldFailWhenTimeoutIsZero() {
        SshConnectionRequest request = new SshConnectionRequest(
                "app.demo.test",
                22,
                "app",
                "password",
                null,
                Duration.ZERO
        );

        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> validator.validate(request)
        );

        assertEquals("SSH connection timeout must not be zero.", exception.getMessage());
    }
}