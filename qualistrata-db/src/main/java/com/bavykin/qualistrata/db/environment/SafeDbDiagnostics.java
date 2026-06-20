package com.bavykin.qualistrata.db.environment;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds safe diagnostic messages for database environment checks.
 *
 * <p>Database drivers may include connection URLs, usernames, host names, SQL
 * fragments, or other infrastructure details in exception messages. Environment
 * check results are intended to be written to reports, so they must not expose
 * raw driver messages by default. This helper keeps public result messages
 * stable and stores only low-risk structured metadata such as exception type and
 * JDBC/Oracle error identifiers.</p>
 */
final class SafeDbDiagnostics {

    /** Oracle diagnostic code format commonly embedded in Oracle JDBC messages. */
    private static final Pattern ORA_CODE_PATTERN = Pattern.compile("ORA-\\d{5}");

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
     * <p>JDBC diagnostic identifiers are safe and useful for troubleshooting, so
     * this method extracts them when a {@link SQLException} is present anywhere in
     * the exception chain:</p>
     *
     * <ul>
     *     <li>{@code sqlState} - vendor-neutral SQL state when provided by the driver;</li>
     *     <li>{@code sqlErrorCode} - vendor-specific numeric JDBC error code;</li>
     *     <li>{@code oraCode} - Oracle {@code ORA-xxxxx} code parsed from the exception chain.</li>
     * </ul>
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

        SQLException sqlException = findSqlException(exception);
        if (sqlException != null) {
            putIfHasText(details, "sqlState", sqlException.getSQLState());
            details.put("sqlErrorCode", Integer.toString(sqlException.getErrorCode()));
        }

        String oraCode = findOraCode(exception);
        putIfHasText(details, "oraCode", oraCode);

        return Map.copyOf(details);
    }

    /**
     * Finds the first {@link SQLException} in the throwable chain.
     *
     * <p>For {@link SQLException}, the JDBC {@code nextException} chain is also
     * inspected because some drivers put the most useful diagnostic information
     * there instead of in the regular cause chain.</p>
     *
     * @param throwable root throwable to inspect.
     * @return first SQL exception, or {@code null} when no SQL exception is found.
     */
    private static SQLException findSqlException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                return sqlException;
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * Finds the first Oracle {@code ORA-xxxxx} diagnostic code in the throwable chain.
     *
     * <p>The method parses only the stable Oracle code and never returns the full
     * exception message, because the message may contain usernames, hosts, JDBC
     * URLs, or other environment-sensitive information.</p>
     *
     * @param throwable root throwable to inspect.
     * @return first Oracle code, or {@code null} when none is present.
     */
    private static String findOraCode(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String code = oraCodeFrom(current.getMessage());
            if (code != null) {
                return code;
            }

            if (current instanceof SQLException sqlException) {
                SQLException next = sqlException.getNextException();
                while (next != null) {
                    code = oraCodeFrom(next.getMessage());
                    if (code != null) {
                        return code;
                    }
                    next = next.getNextException();
                }
            }

            current = current.getCause();
        }
        return null;
    }

    /**
     * Extracts an Oracle {@code ORA-xxxxx} code from text.
     *
     * @param text text to inspect.
     * @return Oracle code, or {@code null} when the text does not contain one.
     */
    private static String oraCodeFrom(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = ORA_CODE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * Adds a value to the details map only when it is not blank.
     *
     * @param details mutable details map.
     * @param key detail key.
     * @param value detail value.
     */
    private static void putIfHasText(Map<String, String> details, String key, String value) {
        if (value != null && !value.isBlank()) {
            details.put(key, value);
        }
    }
}
