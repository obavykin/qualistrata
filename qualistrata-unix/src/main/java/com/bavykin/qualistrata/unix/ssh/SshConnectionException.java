package com.bavykin.qualistrata.unix.ssh;

import com.bavykin.qualistrata.unix.session.UnixSessionException;

/**
 * Exception thrown when SSH connection creation, command execution, or
 * connection lifecycle operation fails at framework or infrastructure level.
 *
 * <p>This exception represents SSH transport-level failures. Normal command
 * execution results, including non-zero exit codes, should still be represented
 * by command result objects.</p>
 */
public class SshConnectionException extends UnixSessionException {

    /**
     * Creates an SSH connection exception with a message.
     *
     * @param message exception message.
     */
    public SshConnectionException(String message) {
        super(message);
    }

    /**
     * Creates an SSH connection exception with a message and cause.
     *
     * @param message exception message.
     * @param cause original exception.
     */
    public SshConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}