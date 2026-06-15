package com.bavykin.qualistrata.unix.ssh;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.session.UnixHostSession;

/**
 * Unix host session backed by an SSH connection.
 *
 * <p>This class adapts the low-level {@link SshConnection} abstraction to the
 * higher-level {@link UnixHostSession} contract. It does not know which SSH
 * library is used underneath.</p>
 */
public class SshUnixHostSession implements UnixHostSession {

    private final SshConnection connection;

    /**
     * Creates an SSH-backed Unix host session.
     *
     * @param connection opened SSH connection used by this session.
     */
    public SshUnixHostSession(SshConnection connection) {
        if (connection == null) {
            throw new SshConnectionException("SSH connection must not be null.");
        }

        this.connection = connection;
    }

    /**
     * Executes a Unix command through the underlying SSH connection.
     *
     * @param request command execution request.
     * @return command execution result.
     */
    @Override
    public CommandResult execute(UnixCommandRequest request) {
        return connection.execute(request);
    }

    /**
     * Checks whether the underlying SSH connection is open.
     *
     * @return {@code true} when the SSH connection is open.
     */
    @Override
    public boolean isOpen() {
        return connection.isOpen();
    }

    /**
     * Closes the underlying SSH connection.
     */
    @Override
    public void close() {
        connection.close();
    }
}