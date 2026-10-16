package com.bavykin.qualistrata.report.writer;

/**
 * Indicates that a test-run artifact could not be serialized or written.
 */
public final class ReportWriteException extends RuntimeException {

    /**
     * Creates a report-write exception.
     *
     * @param message contextual failure message
     * @param cause root cause
     */
    public ReportWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
