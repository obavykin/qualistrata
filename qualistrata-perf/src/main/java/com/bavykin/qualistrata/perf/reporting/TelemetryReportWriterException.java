package com.bavykin.qualistrata.perf.reporting;

/**
 * Runtime exception thrown when local telemetry report writing fails.
 */
public class TelemetryReportWriterException extends RuntimeException {

    /**
     * Creates exception with message and cause.
     *
     * @param message failure description.
     * @param cause root cause.
     */
    public TelemetryReportWriterException(String message, Throwable cause) {
        super(message, cause);
    }
}
