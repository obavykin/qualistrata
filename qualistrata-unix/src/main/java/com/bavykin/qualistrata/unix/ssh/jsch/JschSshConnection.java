package com.bavykin.qualistrata.unix.ssh.jsch;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.command.UnixCommandRequestValidator;
import com.bavykin.qualistrata.unix.ssh.SshConnection;
import com.bavykin.qualistrata.unix.ssh.SshConnectionException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * JSch-based implementation of {@link SshConnection}.
 *
 * <p>The connection owns an already opened JSch {@link Session}. Each command
 * execution opens a dedicated {@code exec} channel, waits for command
 * completion, reads stdout and stderr, and then disconnects the channel.</p>
 *
 * <p>Non-zero command exit codes are returned as normal {@link CommandResult}
 * values. Transport-level failures, timeouts, interrupted execution, and output
 * reading failures are reported as {@link SshConnectionException}.</p>
 */
public class JschSshConnection implements SshConnection {

    private static final long POLL_INTERVAL_MILLIS = 50L;

    private final Session session;
    private final UnixCommandRequestValidator requestValidator;

    /**
     * Creates a JSch SSH connection wrapper.
     *
     * @param session opened JSch session.
     */
    public JschSshConnection(Session session) {
        this(session, new UnixCommandRequestValidator());
    }

    /**
     * Creates a JSch SSH connection wrapper with explicitly provided
     * collaborators.
     *
     * @param session opened JSch session.
     * @param requestValidator command request validator.
     */
    JschSshConnection(
            Session session,
            UnixCommandRequestValidator requestValidator
    ) {
        if (session == null) {
            throw new SshConnectionException("JSch session must not be null.");
        }

        if (requestValidator == null) {
            throw new SshConnectionException("Unix command request validator must not be null.");
        }

        this.session = session;
        this.requestValidator = requestValidator;
    }

    /**
     * Executes a Unix command through a JSch exec channel.
     *
     * @param request command execution request.
     * @return command execution result.
     */
    @Override
    public CommandResult execute(UnixCommandRequest request) {
        requestValidator.validate(request);
        ensureOpen();

        ChannelExec channel = openExecChannel(request.command());

        try {
            channel.connect();
            waitForCompletion(channel, request.timeout());

            String stdout = new String(channel.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(channel.getErrStream().readAllBytes(), StandardCharsets.UTF_8);

            return new CommandResult(channel.getExitStatus(), stdout, stderr);
        } catch (JSchException exception) {
            throw new SshConnectionException("Failed to execute SSH command: " + request.command(), exception);
        } catch (IOException exception) {
            throw new SshConnectionException("Failed to read SSH command output.", exception);
        } finally {
            channel.disconnect();
        }
    }

    /**
     * Checks whether the wrapped JSch session is connected.
     *
     * @return {@code true} when the session is connected.
     */
    @Override
    public boolean isOpen() {
        return session.isConnected();
    }

    /**
     * Disconnects the wrapped JSch session.
     */
    @Override
    public void close() {
        session.disconnect();
    }

    /**
     * Opens a JSch exec channel for command execution.
     *
     * @param command command line to execute.
     * @return opened exec channel before connection.
     */
    private ChannelExec openExecChannel(String command) {
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);

            return channel;
        } catch (JSchException exception) {
            throw new SshConnectionException("Failed to open SSH exec channel.", exception);
        }
    }

    /**
     * Waits until the exec channel is closed or timeout expires.
     *
     * @param channel exec channel.
     * @param timeout optional command timeout.
     */
    private void waitForCompletion(ChannelExec channel, Duration timeout) {
        long deadline = timeout == null
                ? Long.MAX_VALUE
                : System.nanoTime() + timeout.toNanos();

        while (!channel.isClosed()) {
            if (System.nanoTime() > deadline) {
                channel.disconnect();

                throw new SshConnectionException("SSH command timed out after: " + timeout);
            }

            sleepBeforeNextPoll();
        }
    }

    /**
     * Sleeps before the next channel status poll.
     */
    private void sleepBeforeNextPoll() {
        try {
            Thread.sleep(POLL_INTERVAL_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new SshConnectionException("SSH command execution was interrupted.", exception);
        }
    }

    /**
     * Ensures the underlying SSH session is connected.
     */
    private void ensureOpen() {
        if (!isOpen()) {
            throw new SshConnectionException("SSH connection is not open.");
        }
    }
}