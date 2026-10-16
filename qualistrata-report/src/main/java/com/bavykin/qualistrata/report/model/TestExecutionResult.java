package com.bavykin.qualistrata.report.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable framework-neutral result of one concrete test invocation.
 *
 * <p>Retry attempts are intentionally not modeled here yet. This type represents
 * one observed execution result and will later become the building block for the
 * logical-test attempt model.</p>
 *
 * @param test test descriptor
 * @param status normalized execution status
 * @param startedAt execution start timestamp
 * @param finishedAt execution finish timestamp
 * @param failure failure diagnostics, when available
 */
public record TestExecutionResult(
        TestDescriptor test,
        TestStatus status,
        Instant startedAt,
        Instant finishedAt,
        FailureDetails failure
) {

    /**
     * Creates and validates an execution result.
     */
    public TestExecutionResult {
        Objects.requireNonNull(test, "test");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(finishedAt, "finishedAt");

        if (finishedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException(
                    "finishedAt must not be before startedAt"
            );
        }
        if (status == TestStatus.PASSED && failure != null) {
            throw new IllegalArgumentException(
                    "A passed test must not contain failure details"
            );
        }
        if (status == TestStatus.FAILED && failure == null) {
            throw new IllegalArgumentException(
                    "A failed test must contain failure details"
            );
        }
    }

    /**
     * Returns the non-negative elapsed execution time.
     *
     * @return duration between start and finish timestamps
     */
    public Duration duration() {
        return Duration.between(startedAt, finishedAt);
    }

    /**
     * Returns failure diagnostics when they were captured.
     *
     * @return optional failure details
     */
    public Optional<FailureDetails> failureDetails() {
        return Optional.ofNullable(failure);
    }
}
