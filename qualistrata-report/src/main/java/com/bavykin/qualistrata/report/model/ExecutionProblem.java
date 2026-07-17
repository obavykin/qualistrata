package com.bavykin.qualistrata.report.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Failure or abnormal condition that affects the run outside a normal test result.
 *
 * <p>Examples include TestNG configuration failures, adapter failures, fragment
 * read errors, and report writer problems.</p>
 *
 * @param phase lifecycle phase or subsystem where the problem occurred
 * @param occurredAt problem timestamp
 * @param failure serialized failure details
 */
public record ExecutionProblem(
        String phase,
        Instant occurredAt,
        FailureDetails failure
) {

    /**
     * Creates and validates an execution problem.
     */
    public ExecutionProblem {
        phase = requireText(phase);
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(failure, "failure");
    }

    private static String requireText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("phase must not be blank");
        }
        return value.trim();
    }
}
