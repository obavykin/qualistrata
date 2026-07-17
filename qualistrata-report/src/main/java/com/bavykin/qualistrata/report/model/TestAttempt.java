package com.bavykin.qualistrata.report.model;

import java.util.Objects;

/**
 * One ordered execution attempt of a logical test.
 *
 * @param number one-based attempt number
 * @param result observed execution result for this attempt
 */
public record TestAttempt(
        int number,
        TestExecutionResult result
) {

    /**
     * Creates and validates a test attempt.
     */
    public TestAttempt {
        if (number < 1) {
            throw new IllegalArgumentException("Attempt number must be at least 1");
        }
        Objects.requireNonNull(result, "result");
    }
}
