package com.bavykin.qualistrata.unix.file;

import com.bavykin.qualistrata.core.exceptions.FrameworkException;

/**
 * Failure raised by generic remote file operations.
 *
 * <p>The exception is transport-neutral. Concrete implementations may use SFTP,
 * local filesystem adapters, container mounts, or future file-transfer
 * mechanisms underneath.</p>
 */
public class RemoteFileException extends FrameworkException {

    /**
     * Creates remote file exception.
     *
     * @param message error message.
     */
    public RemoteFileException(String message) {
        super(message);
    }

    /**
     * Creates remote file exception with cause.
     *
     * @param message error message.
     * @param cause root cause.
     */
    public RemoteFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
