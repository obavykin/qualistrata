package com.bavykin.qualistrata.unix.ssh.jsch;

import com.bavykin.qualistrata.unix.ssh.SshConnectionException;
import com.bavykin.qualistrata.unix.ssh.SshConnectionRequestValidator;
import com.jcraft.jsch.JSch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link com.bavykin.qualistrata.unix.ssh.jsch.JschSshConnectionFactory}.
 *
 * <p>The tests intentionally avoid opening real SSH connections. Real SSH
 * connectivity must be tested in a separate integration-test layer with
 * explicit infrastructure configuration.</p>
 */
class JschSshConnectionFactoryTest {

    @Test
    void shouldFailWhenJschInstanceIsNull() {
        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> new com.bavykin.qualistrata.unix.ssh.jsch.JschSshConnectionFactory(null, new SshConnectionRequestValidator())
        );

        assertEquals("JSch instance must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenRequestValidatorIsNull() {
        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> new com.bavykin.qualistrata.unix.ssh.jsch.JschSshConnectionFactory(new JSch(), null)
        );

        assertEquals("SSH connection request validator must not be null.", exception.getMessage());
    }
}