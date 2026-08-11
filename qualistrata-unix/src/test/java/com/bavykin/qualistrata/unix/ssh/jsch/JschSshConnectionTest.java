package com.bavykin.qualistrata.unix.ssh.jsch;

import com.bavykin.qualistrata.unix.command.UnixCommandRequestValidator;
import com.bavykin.qualistrata.unix.ssh.SshConnectionException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link JschSshConnection}.
 *
 * <p>The tests avoid real network connections and verify constructor-level
 * validation only. Command execution over real SSH belongs to integration
 * tests.</p>
 */
class JschSshConnectionTest {

    @Test
    void shouldTerminateInteractiveResponsesWithUnixLineFeed() {
        assertArrayEquals(
                "secret\n".getBytes(StandardCharsets.UTF_8),
                JschSshConnection.encodeInteractiveResponse("secret")
        );
    }

    @Test
    void shouldBuildInteractiveTranscriptFromStdoutAndStderr() {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        stdout.writeBytes("stdout-prompt".getBytes(StandardCharsets.UTF_8));
        stderr.writeBytes("stderr-prompt".getBytes(StandardCharsets.UTF_8));

        assertEquals(
                "stdout-prompt\nstderr-prompt",
                JschSshConnection.buildInteractiveTranscript(stdout, stderr)
        );
    }

    @Test
    void shouldFailWhenSessionIsNull() {
        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> new JschSshConnection(null)
        );

        assertEquals("JSch session must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenCommandRequestValidatorIsNull() throws JSchException {
        Session session = new JSch().getSession("user", "example.test", 22);

        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> new JschSshConnection(session, null)
        );

        assertEquals("Unix command request validator must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenConnectionIsNotOpen() throws JSchException {
        Session session = new JSch().getSession("user", "example.test", 22);
        JschSshConnection connection = new JschSshConnection(
                session,
                new UnixCommandRequestValidator()
        );

        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> connection.execute(new com.bavykin.qualistrata.unix.command.UnixCommandRequest(
                        "hostname",
                        java.time.Duration.ofSeconds(5)
                ))
        );

        assertEquals("SSH connection is not open.", exception.getMessage());
    }
}