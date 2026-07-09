package com.bavykin.qualistrata.unix.interactive;

/**
 * Runtime exception raised when an interactive Unix command cannot be
 * validated, executed, or completed successfully at the transport level.
 */
public class InteractiveCommandException extends RuntimeException {

    /**
     * Creates an exception with a message.
     *
     * @param message failure description.
     */
    public InteractiveCommandException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and root cause.
     *
     * @param message failure description.
     * @param cause root cause.
     */
    public InteractiveCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
