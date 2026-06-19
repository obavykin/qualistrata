package com.bavykin.qualistrata.db.environment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds safe diagnostic messages for database environment checks.
 *
 * <p>Database drivers may include connection URLs, usernames, host names, SQL
 * fragments, or other infrastructure details in exception messages. Environment
 * check results are intended to be written to reports, so they must not expose
 * raw driver messages by default. This helper keeps public result messages
 * stable and stores only low-risk structured metadata such as exception type.</p>
 */
final class SafeDbDiagnostics {

    private SafeDbDiagnostics() {
        // Utility class.
    }

    /**
     * Creates a stable failure message that does not include raw exception text.
     *
     * @param checkFailureMessage high-level check failure message.
     * @return safe message suitable for logs and reports.
     */
    static String failureMessage(String checkFailureMessage) {
        if (checkFailureMessage == null || checkFailureMessage.isBlank()) {
            return "Database environment check failed.";
        }
        return checkFailureMessage.endsWith(".")
                ? checkFailureMessage
                : checkFailureMessage + ".";
    }

    /**
     * Creates safe structured failure details from an exception.
     *
     * <p>The original exception message is intentionally not included because it
     * may contain connection strings, usernames, host names, or driver-specific
     * diagnostics that should not be published in generated reports.</p>
     *
     * @param exception exception thrown by the provider or DB client.
     * @return immutable map with safe failure metadata.
     */
    static Map<String, String> failureDetails(Exception exception) {
        Map<String, String> details = new LinkedHashMap<>();

        if (exception == null) {
            details.put("exceptionType", "unknown");
            return Map.copyOf(details);
        }

        details.put("exceptionType", exception.getClass().getName());

        Throwable cause = exception.getCause();
        if (cause != null) {
            details.put("causeType", cause.getClass().getName());
        }

        return Map.copyOf(details);
    }
}
