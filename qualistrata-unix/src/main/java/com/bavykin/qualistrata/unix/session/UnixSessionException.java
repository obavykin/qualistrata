package com.bavykin.qualistrata.unix.session;

import com.bavykin.qualistrata.core.exceptions.FrameworkException;

/**
 * Exception thrown when Unix session creation, borrowing, returning, or
 * invalidation fails at framework or infrastructure level.
 *
 * <p>This exception represents session lifecycle failures. Normal command
 * execution results, including non-zero exit codes, should still be represented
 * by command execution result objects.</p>
 */
public class UnixSessionException extends FrameworkException {

    /**
     * Creates a Unix session exception with a message.
     *
     * @param message exception message.
     */
    public UnixSessionException(String message) {
        super(message);
    }

    /**
     * Creates a Unix session exception with a message and cause.
     *
     * @param message exception message.
     * @param cause original exception.
     */
    public UnixSessionException(String message, Throwable cause) {
        super(message, cause);
    }
}