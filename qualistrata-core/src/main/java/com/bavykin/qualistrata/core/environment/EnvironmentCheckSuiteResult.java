package com.bavykin.qualistrata.core.environment;

import java.time.Duration;
import java.util.List;

/**
 * Aggregated result of an environment check suite.
 *
 * <p>The suite result groups multiple individual check results and provides
 * convenience methods for evaluating overall readiness.</p>
 *
 * @param name suite name.
 * @param results individual check results.
 * @param duration total suite execution duration.
 */
public record EnvironmentCheckSuiteResult(
        String name,
        List<EnvironmentCheckResult> results,
        Duration duration
) {

    /**
     * Creates a validated environment check suite result.
     *
     * @param name suite name.
     * @param results individual check results.
     * @param duration total suite execution duration.
     */
    public EnvironmentCheckSuiteResult {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Environment check suite name must not be blank.");
        }

        results = results == null
                ? List.of()
                : List.copyOf(results);

        if (duration != null && duration.isNegative()) {
            throw new IllegalArgumentException("Environment check suite duration must not be negative.");
        }
    }

    /**
     * Checks whether all checks passed.
     *
     * @return {@code true} when all checks passed and the suite is not empty.
     */
    public boolean allPassed() {
        return !results.isEmpty() && results.stream().allMatch(EnvironmentCheckResult::isPassed);
    }

    /**
     * Checks whether the suite contains at least one failed check.
     *
     * @return {@code true} when at least one check failed.
     */
    public boolean hasFailures() {
        return results.stream().anyMatch(EnvironmentCheckResult::isFailed);
    }

    /**
     * Checks whether the suite contains at least one blocking failure.
     *
     * @return {@code true} when at least one check is a failed error-level check.
     */
    public boolean hasBlockingFailures() {
        return results.stream().anyMatch(EnvironmentCheckResult::isBlockingFailure);
    }

    /**
     * Counts passed checks.
     *
     * @return number of passed checks.
     */
    public long passedCount() {
        return results.stream().filter(EnvironmentCheckResult::isPassed).count();
    }

    /**
     * Counts failed checks.
     *
     * @return number of failed checks.
     */
    public long failedCount() {
        return results.stream().filter(EnvironmentCheckResult::isFailed).count();
    }

    /**
     * Counts skipped checks.
     *
     * @return number of skipped checks.
     */
    public long skippedCount() {
        return results.stream().filter(EnvironmentCheckResult::isSkipped).count();
    }
}