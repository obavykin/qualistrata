package com.bavykin.qualistrata.unix.ssh.jsch;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.command.UnixCommandRequestValidator;
import com.bavykin.qualistrata.unix.ssh.SshConnection;
import com.bavykin.qualistrata.unix.ssh.SshConnectionException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * JSch-based implementation of {@link SshConnection}.
 *
 * <p>The connection owns an already opened JSch {@link Session}. Each command
 * execution opens a dedicated {@code exec} channel, waits for command
 * completion, drains stdout and stderr, and then disconnects the channel.</p>
 *
 * <p>Non-zero command exit codes are returned as normal {@link CommandResult}
 * values. Transport-level failures, timeouts, interrupted execution, and output
 * reading failures are reported as {@link SshConnectionException}.</p>
 */
public class JschSshConnection implements SshConnection {

    private static final long POLL_INTERVAL_MILLIS = 50L;
    private static final int DEFAULT_CHANNEL_CONNECT_TIMEOUT_MILLIS = 10_000;

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
            InputStream stdoutStream = channel.getInputStream();
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            channel.setErrStream(stderr);
            channel.connect(resolveChannelConnectTimeoutMillis(request.timeout()));

            waitForCompletionAndDrainOutput(channel, stdoutStream, stdout, request.timeout());
            drainAvailableOutput(stdoutStream, stdout);

            return new CommandResult(
                    channel.getExitStatus(),
                    stdout.toString(StandardCharsets.UTF_8),
                    stderr.toString(StandardCharsets.UTF_8)
            );
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
     * @return exec channel before connection.
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
     * Waits until the exec channel is closed or timeout expires while draining
     * available stdout data.
     *
     * @param channel exec channel.
     * @param stdoutStream stdout stream.
     * @param stdout stdout buffer.
     * @param timeout optional command timeout.
     * @throws IOException if stdout cannot be read.
     */
    private void waitForCompletionAndDrainOutput(
            ChannelExec channel,
            InputStream stdoutStream,
            ByteArrayOutputStream stdout,
            Duration timeout
    ) throws IOException {
        long deadline = timeout == null
                ? Long.MAX_VALUE
                : System.nanoTime() + timeout.toNanos();

        while (!channel.isClosed()) {
            drainAvailableOutput(stdoutStream, stdout);

            if (System.nanoTime() > deadline) {
                channel.disconnect();

                throw new SshConnectionException("SSH command timed out after: " + timeout);
            }

            sleepBeforeNextPoll();
        }
    }

    /**
     * Reads all currently available stdout bytes without blocking for EOF.
     *
     * @param stream stdout stream.
     * @param output output buffer.
     * @throws IOException if stdout cannot be read.
     */
    private void drainAvailableOutput(
            InputStream stream,
            ByteArrayOutputStream output
    ) throws IOException {
        byte[] buffer = new byte[8192];

        while (stream.available() > 0) {
            int read = stream.read(buffer, 0, Math.min(buffer.length, stream.available()));

            if (read < 0) {
                return;
            }

            output.write(buffer, 0, read);
        }
    }

    /**
     * Resolves channel connection timeout.
     *
     * @param commandTimeout optional command timeout.
     * @return timeout in milliseconds.
     */
    private int resolveChannelConnectTimeoutMillis(Duration commandTimeout) {
        if (commandTimeout == null) {
            return DEFAULT_CHANNEL_CONNECT_TIMEOUT_MILLIS;
        }

        return Math.toIntExact(commandTimeout.toMillis());
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