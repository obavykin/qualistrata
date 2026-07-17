package com.bavykin.qualistrata.report.model;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Immutable result of one logical test with one or more ordered attempts.
 *
 * <p>All attempts must belong to the same stable {@link TestIdentifier}.
 * Previous failures remain available even when a later retry passes.</p>
 *
 * @param test logical test descriptor
 * @param attempts ordered, contiguous attempt history starting at one
 * @param finalStatusPolicy policy used to calculate the final status
 */
public record LogicalTestResult(
        TestDescriptor test,
        List<TestAttempt> attempts,
        FinalStatusPolicy finalStatusPolicy
) {

    /**
     * Creates and validates a logical test result.
     */
    public LogicalTestResult {
        Objects.requireNonNull(test, "test");
        Objects.requireNonNull(finalStatusPolicy, "finalStatusPolicy");

        if (attempts == null || attempts.isEmpty()) {
            throw new IllegalArgumentException("At least one attempt is required");
        }

        attempts = List.copyOf(attempts);
        validateAttempts(test, attempts);
    }

    /**
     * Creates a logical result using the canonical last-attempt policy.
     *
     * @param test logical test descriptor
     * @param attempts ordered attempt history
     * @return logical result using {@link FinalStatusPolicy#LAST_ATTEMPT}
     */
    public static LogicalTestResult of(
            TestDescriptor test,
            List<TestAttempt> attempts
    ) {
        return new LogicalTestResult(
                test,
                attempts,
                FinalStatusPolicy.LAST_ATTEMPT
        );
    }

    /**
     * Returns the final status calculated from the complete attempt history.
     *
     * @return final logical-test status
     */
    public TestStatus finalStatus() {
        return finalStatusPolicy.resolve(attempts);
    }

    /**
     * Returns the sum of all attempt durations.
     *
     * @return total observed execution duration
     */
    public Duration totalDuration() {
        return attempts.stream()
                .map(attempt -> attempt.result().duration())
                .reduce(Duration.ZERO, Duration::plus);
    }

    /**
     * Returns whether the logical test was executed more than once.
     *
     * @return {@code true} when more than one attempt exists
     */
    public boolean retried() {
        return attempts.size() > 1;
    }

    private static void validateAttempts(
            TestDescriptor test,
            List<TestAttempt> attempts
    ) {
        TestIdentifier expectedIdentifier = test.identifier();

        for (int index = 0; index < attempts.size(); index++) {
            TestAttempt attempt = Objects.requireNonNull(
                    attempts.get(index),
                    "attempts[" + index + "]"
            );
            int expectedNumber = index + 1;

            if (attempt.number() != expectedNumber) {
                throw new IllegalArgumentException(
                        "Attempt numbers must be contiguous and ordered from 1; "
                                + "expected " + expectedNumber
                                + " but was " + attempt.number()
                );
            }

            TestIdentifier actualIdentifier =
                    attempt.result().test().identifier();

            if (!expectedIdentifier.equals(actualIdentifier)) {
                throw new IllegalArgumentException(
                        "All attempts must use the logical test identifier "
                                + expectedIdentifier.uniqueId()
                );
            }
        }
    }
}
