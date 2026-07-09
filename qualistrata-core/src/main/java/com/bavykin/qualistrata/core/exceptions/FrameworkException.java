package com.bavykin.qualistrata.core.exceptions;

/**
 * Base unchecked exception for QualiStrata framework-level failures.
 *
 * <p>This exception is intended for errors caused by framework configuration,
 * infrastructure setup, integration boundaries, or invalid framework usage.
 * Product-specific assertions and business validation failures should use more
 * specific exception types where appropriate.</p>
 */
public class FrameworkException extends RuntimeException {

    /**
     * Creates a framework exception with a descriptive message.
     *
     * @param message explanation of the framework failure.
     */
    public FrameworkException(String message) {
        super(message);
    }

    /**
     * Creates a framework exception with a descriptive message and root cause.
     *
     * @param message explanation of the framework failure.
     * @param cause original exception that caused this failure.
     */
    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}