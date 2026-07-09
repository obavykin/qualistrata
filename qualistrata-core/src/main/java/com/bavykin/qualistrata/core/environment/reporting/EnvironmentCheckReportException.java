package com.bavykin.qualistrata.core.environment.reporting;

import com.bavykin.qualistrata.core.exceptions.FrameworkException;

/**
 * Exception thrown when an environment check report cannot be written.
 */
public class EnvironmentCheckReportException extends FrameworkException {

    /**
     * Creates an environment check report exception.
     *
     * @param message exception message.
     */
    public EnvironmentCheckReportException(String message) {
        super(message);
    }

    /**
     * Creates an environment check report exception.
     *
     * @param message exception message.
     * @param cause original cause.
     */
    public EnvironmentCheckReportException(String message, Throwable cause) {
        super(message, cause);
    }
}