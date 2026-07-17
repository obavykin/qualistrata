package com.bavykin.qualistrata.report.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Stable identity and source information for one test run.
 *
 * @param runId stable run identifier
 * @param source source task, suite, or execution name
 * @param startedAt run start timestamp
 */
public record TestRunDescriptor(
        String runId,
        String source,
        Instant startedAt
) {

    /**
     * Creates and validates a test-run descriptor.
     */
    public TestRunDescriptor {
        runId = requireText(runId, "runId");
        source = requireText(source, "source");
        Objects.requireNonNull(startedAt, "startedAt");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
