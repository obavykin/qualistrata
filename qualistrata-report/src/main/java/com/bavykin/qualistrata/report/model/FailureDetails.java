package com.bavykin.qualistrata.report.model;

/**
 * Safe, serializable representation of a test or infrastructure failure.
 *
 * <p>The model stores textual diagnostics rather than a {@link Throwable} so it
 * can be written to fragments, aggregated across JVMs, and rendered without
 * retaining framework or class-loader state.</p>
 *
 * @param exceptionType fully qualified exception type, when available
 * @param message failure message, when available
 * @param stackTrace rendered stack trace, when collection is enabled
 */
public record FailureDetails(
        String exceptionType,
        String message,
        String stackTrace
) {

    /**
     * Creates normalized failure details.
     */
    public FailureDetails {
        exceptionType = normalize(exceptionType);
        message = normalize(message);
        stackTrace = normalize(stackTrace);

        if (exceptionType == null && message == null && stackTrace == null) {
            throw new IllegalArgumentException(
                    "At least one failure detail must be provided"
            );
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
