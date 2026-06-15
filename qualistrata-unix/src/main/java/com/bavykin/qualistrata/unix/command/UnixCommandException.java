package com.bavykin.qualistrata.unix.command;

import com.bavykin.qualistrata.core.exceptions.FrameworkException;

/**
 * Exception thrown when Unix command execution fails at framework or
 * infrastructure level.
 *
 * <p>This exception should not be used for normal non-zero command exit codes.
 * A command may return exit code {@code 1}, {@code 2}, or any other value as a
 * valid process result. Such values should be represented by
 * {@code CommandResult} and interpreted by the caller.</p>
 */
public class UnixCommandException extends FrameworkException {

    /**
     * Creates a Unix command exception with a message.
     *
     * @param message exception message.
     */
    public UnixCommandException(String message) {
        super(message);
    }

    /**
     * Creates a Unix command exception with a message and cause.
     *
     * @param message exception message.
     * @param cause original exception.
     */
    public UnixCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}