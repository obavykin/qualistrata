package com.bavykin.qualistrata.unix.ssh;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;

/**
 * Open low-level SSH connection.
 *
 * <p>This abstraction hides the concrete SSH library used underneath. Future
 * implementations may be based on JSch, Apache MINA SSHD, SSHJ, or another
 * transport library without changing higher-level Unix session contracts.</p>
 *
 * <p>The connection is responsible for transport-level command execution only.
 * Pooling and host/user session lifecycle are handled by higher-level Unix
 * session abstractions.</p>
 */
public interface SshConnection extends AutoCloseable {

    /**
     * Executes a Unix command through this SSH connection.
     *
     * @param request command execution request.
     * @return command result containing exit code, stdout, and stderr.
     * @throws SshConnectionException if the command cannot be executed because
     *                                of SSH transport-level failure.
     */
    CommandResult execute(UnixCommandRequest request);

    /**
     * Checks whether the SSH connection is currently open.
     *
     * @return {@code true} when the connection is open.
     */
    boolean isOpen();

    /**
     * Closes this SSH connection.
     */
    @Override
    void close();
}