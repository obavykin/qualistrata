package com.bavykin.qualistrata.core.environment;

import java.time.Duration;
import java.util.Map;

/**
 * Result of a single environment readiness check.
 *
 * <p>The result is intentionally generic. It does not contain product-specific
 * fields and can be used by core, Unix, API, DB, UI, or project-specific
 * environment checks.</p>
 *
 * @param id stable check identifier.
 * @param name human-readable check name.
 * @param status check execution status.
 * @param severity check severity.
 * @param message human-readable result message.
 * @param duration check execution duration.
 * @param details optional diagnostic details.
 * @param error optional error captured during check execution.
 */
public record EnvironmentCheckResult(
        String id,
        String name,
        EnvironmentCheckStatus status,
        EnvironmentCheckSeverity severity,
        String message,
        Duration duration,
        Map<String, String> details,
        Throwable error
) {

    /**
     * Creates a validated environment check result.
     *
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param status check execution status.
     * @param severity check severity.
     * @param message human-readable result message.
     * @param duration check execution duration.
     * @param details optional diagnostic details.
     * @param error optional error captured during check execution.
     */
    public EnvironmentCheckResult {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Environment check id must not be blank.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Environment check name must not be blank.");
        }

        if (status == null) {
            throw new IllegalArgumentException("Environment check status must not be null.");
        }

        if (severity == null) {
            throw new IllegalArgumentException("Environment check severity must not be null.");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Environment check message must not be blank.");
        }

        if (duration != null && duration.isNegative()) {
            throw new IllegalArgumentException("Environment check duration must not be negative.");
        }

        details = details == null
                ? Map.of()
                : Map.copyOf(details);
    }

    /**
     * Creates a passed check result with {@link EnvironmentCheckSeverity#INFO}
     * severity.
     *
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param message human-readable result message.
     * @return passed check result.
     */
    public static EnvironmentCheckResult passed(
            String id,
            String name,
            String message
    ) {
        return passed(id, name, message, null, Map.of());
    }

    /**
     * Creates a passed check result with {@link EnvironmentCheckSeverity#INFO}
     * severity.
     *
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param message human-readable result message.
     * @param duration check execution duration.
     * @param details optional diagnostic details.
     * @return passed check result.
     */
    public static EnvironmentCheckResult passed(
            String id,
            String name,
            String message,
            Duration duration,
            Map<String, String> details
    ) {
        return new EnvironmentCheckResult(
                id,
                name,
                EnvironmentCheckStatus.PASSED,
                EnvironmentCheckSeverity.INFO,
                message,
                duration,
                details,
                null
        );
    }

    /**
     * Creates a failed check result.
     *
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity.
     * @param message human-readable result message.
     * @return failed check result.
     */
    public static EnvironmentCheckResult failed(
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String message
    ) {
        return failed(id, name, severity, message, null, Map.of(), null);
    }

    /**
     * Creates a failed check result.
     *
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity.
     * @param message human-readable result message.
     * @param duration check execution duration.
     * @param details optional diagnostic details.
     * @param error optional error captured during check execution.
     * @return failed check result.
     */
    public static EnvironmentCheckResult failed(
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String message,
            Duration duration,
            Map<String, String> details,
            Throwable error
    ) {
        return new EnvironmentCheckResult(
                id,
                name,
                EnvironmentCheckStatus.FAILED,
                severity,
                message,
                duration,
                details,
                error
        );
    }

    /**
     * Creates a skipped check result.
     *
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity.
     * @param message human-readable skip reason.
     * @return skipped check result.
     */
    public static EnvironmentCheckResult skipped(
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String message
    ) {
        return new EnvironmentCheckResult(
                id,
                name,
                EnvironmentCheckStatus.SKIPPED,
                severity,
                message,
                null,
                Map.of(),
                null
        );
    }

    /**
     * Checks whether this result has passed.
     *
     * @return {@code true} when status is {@link EnvironmentCheckStatus#PASSED}.
     */
    public boolean isPassed() {
        return status == EnvironmentCheckStatus.PASSED;
    }

    /**
     * Checks whether this result has failed.
     *
     * @return {@code true} when status is {@link EnvironmentCheckStatus#FAILED}.
     */
    public boolean isFailed() {
        return status == EnvironmentCheckStatus.FAILED;
    }

    /**
     * Checks whether this result was skipped.
     *
     * @return {@code true} when status is {@link EnvironmentCheckStatus#SKIPPED}.
     */
    public boolean isSkipped() {
        return status == EnvironmentCheckStatus.SKIPPED;
    }

    /**
     * Checks whether this result should be treated as blocking by default.
     *
     * <p>Only failed checks with {@link EnvironmentCheckSeverity#ERROR} severity
     * are considered blocking by the generic model. Consuming projects may apply
     * stricter or more relaxed policies.</p>
     *
     * @return {@code true} when this result is a failed error-level check.
     */
    public boolean isBlockingFailure() {
        return isFailed() && severity == EnvironmentCheckSeverity.ERROR;
    }
}