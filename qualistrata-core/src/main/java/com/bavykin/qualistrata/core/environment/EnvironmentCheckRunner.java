package com.bavykin.qualistrata.core.environment;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Runner for executing environment check suites.
 *
 * <p>The runner executes check definitions sequentially, measures execution
 * duration, and aggregates individual check results into
 * {@link EnvironmentCheckSuiteResult}.</p>
 *
 * <p>Environment checks are encouraged to return failed results for expected
 * environment readiness problems. If a check throws an unexpected exception,
 * the runner converts it into a failed result using the definition metadata.</p>
 */
public class EnvironmentCheckRunner {

    /**
     * Creates an environment check runner.
     */
    public EnvironmentCheckRunner() {
        // Default runner has no configurable state.
    }

    /**
     * Runs the provided environment check definitions.
     *
     * @param suiteName human-readable suite name.
     * @param definitions environment check definitions.
     * @return aggregated environment check suite result.
     */
    public EnvironmentCheckSuiteResult run(
            String suiteName,
            List<EnvironmentCheckDefinition> definitions
    ) {
        if (suiteName == null || suiteName.isBlank()) {
            throw new IllegalArgumentException("Environment check suite name must not be blank.");
        }

        List<EnvironmentCheckDefinition> safeDefinitions = definitions == null
                ? List.of()
                : List.copyOf(definitions);

        long suiteStartNanos = System.nanoTime();
        List<EnvironmentCheckResult> results = new ArrayList<>();

        for (EnvironmentCheckDefinition definition : safeDefinitions) {
            results.add(runSingle(definition));
        }

        return new EnvironmentCheckSuiteResult(
                suiteName,
                results,
                elapsedSince(suiteStartNanos)
        );
    }

    /**
     * Runs one environment check definition.
     *
     * @param definition check definition.
     * @return check result.
     */
    private EnvironmentCheckResult runSingle(EnvironmentCheckDefinition definition) {
        long checkStartNanos = System.nanoTime();

        try {
            EnvironmentCheckResult result = definition.check().check();

            if (result == null) {
                return failedBecauseReturnedNull(definition, elapsedSince(checkStartNanos));
            }

            return resultWithMeasuredDurationWhenMissing(
                    result,
                    elapsedSince(checkStartNanos)
            );
        } catch (RuntimeException exception) {
            return failedBecauseExceptionWasThrown(
                    definition,
                    elapsedSince(checkStartNanos),
                    exception
            );
        }
    }

    /**
     * Adds measured duration to a result when the check did not provide one.
     *
     * @param result original check result.
     * @param measuredDuration measured execution duration.
     * @return original or duration-enriched check result.
     */
    private EnvironmentCheckResult resultWithMeasuredDurationWhenMissing(
            EnvironmentCheckResult result,
            Duration measuredDuration
    ) {
        if (result.duration() != null) {
            return result;
        }

        return new EnvironmentCheckResult(
                result.id(),
                result.name(),
                result.status(),
                result.severity(),
                result.message(),
                measuredDuration,
                result.details(),
                result.error()
        );
    }

    /**
     * Creates a failed result when a check unexpectedly returns {@code null}.
     *
     * @param definition check definition.
     * @param duration measured execution duration.
     * @return failed check result.
     */
    private EnvironmentCheckResult failedBecauseReturnedNull(
            EnvironmentCheckDefinition definition,
            Duration duration
    ) {
        return EnvironmentCheckResult.failed(
                definition.id(),
                definition.name(),
                definition.severity(),
                "Environment check returned null result.",
                duration,
                Map.of(),
                null
        );
    }

    /**
     * Creates a failed result when a check throws an unexpected exception.
     *
     * @param definition check definition.
     * @param duration measured execution duration.
     * @param exception thrown exception.
     * @return failed check result.
     */
    private EnvironmentCheckResult failedBecauseExceptionWasThrown(
            EnvironmentCheckDefinition definition,
            Duration duration,
            RuntimeException exception
    ) {
        return EnvironmentCheckResult.failed(
                definition.id(),
                definition.name(),
                definition.severity(),
                "Environment check failed with unexpected exception: " + exception.getMessage(),
                duration,
                Map.of(
                        "exceptionType",
                        exception.getClass().getName()
                ),
                exception
        );
    }

    /**
     * Calculates elapsed duration since the given monotonic timestamp.
     *
     * @param startNanos start timestamp from {@link System#nanoTime()}.
     * @return elapsed duration.
     */
    private Duration elapsedSince(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }
}