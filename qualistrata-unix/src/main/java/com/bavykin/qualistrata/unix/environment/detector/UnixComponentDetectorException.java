package com.bavykin.qualistrata.unix.environment.detector;

import com.bavykin.qualistrata.unix.session.UnixSessionException;

/**
 * Exception thrown when Unix component detector configuration or execution is
 * invalid.
 *
 * <p>Detector exceptions are generic and must not contain product-specific
 * assumptions. Environment checks should usually convert these exceptions into
 * failed {@code EnvironmentCheckResult} instances so reports can still be
 * generated.</p>
 */
public class UnixComponentDetectorException extends UnixSessionException {

    /**
     * Creates a detector exception.
     *
     * @param message diagnostic message.
     */
    public UnixComponentDetectorException(String message) {
        super(message);
    }

    /**
     * Creates a detector exception with a root cause.
     *
     * @param message diagnostic message.
     * @param cause root cause.
     */
    public UnixComponentDetectorException(String message, Throwable cause) {
        super(message, cause);
    }
}