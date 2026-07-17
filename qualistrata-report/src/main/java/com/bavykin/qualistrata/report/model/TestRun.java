package com.bavykin.qualistrata.report.model;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable root model for one complete or partial QualiStrata test run.
 *
 * <p>The summary is always calculated from {@link #tests()} so writers cannot
 * serialize counters that disagree with the logical test results.</p>
 *
 * @param schemaVersion report schema version
 * @param descriptor run identity and source data
 * @param finishedAt run finish timestamp
 * @param complete whether normal adapter lifecycle completion was observed
 * @param metadata safe consumer-supplied run metadata
 * @param tests logical test results
 * @param executionProblems run-level problems outside normal test results
 */
public record TestRun(
        String schemaVersion,
        TestRunDescriptor descriptor,
        Instant finishedAt,
        boolean complete,
        Map<String, Object> metadata,
        List<LogicalTestResult> tests,
        List<ExecutionProblem> executionProblems
) {

    /**
     * Current schema version used by the initial report model.
     */
    public static final String CURRENT_SCHEMA_VERSION = "1.0";

    /**
     * Creates and validates a test-run model.
     */
    public TestRun {
        schemaVersion = requireText(schemaVersion, "schemaVersion");
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(finishedAt, "finishedAt");

        if (finishedAt.isBefore(descriptor.startedAt())) {
            throw new IllegalArgumentException(
                    "finishedAt must not be before run startedAt"
            );
        }

        metadata = immutableMetadata(metadata);
        tests = immutableTests(tests);
        executionProblems = immutableProblems(executionProblems);
    }

    /**
     * Creates a test run using the current schema version.
     *
     * @param descriptor run descriptor
     * @param finishedAt run finish timestamp
     * @param complete whether normal completion was observed
     * @param metadata safe run metadata
     * @param tests logical test results
     * @param executionProblems run-level execution problems
     * @return immutable test run
     */
    public static TestRun current(
            TestRunDescriptor descriptor,
            Instant finishedAt,
            boolean complete,
            Map<String, Object> metadata,
            List<LogicalTestResult> tests,
            List<ExecutionProblem> executionProblems
    ) {
        return new TestRun(
                CURRENT_SCHEMA_VERSION,
                descriptor,
                finishedAt,
                complete,
                metadata,
                tests,
                executionProblems
        );
    }

    /**
     * Returns the run duration.
     *
     * @return duration between run start and finish
     */
    public Duration duration() {
        return Duration.between(descriptor.startedAt(), finishedAt);
    }

    /**
     * Calculates status counts from the logical test results.
     *
     * @return consistent run summary
     */
    public TestRunSummary summary() {
        return TestRunSummary.from(tests);
    }

    private static Map<String, Object> immutableMetadata(
            Map<String, Object> metadata
    ) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }

        LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
        metadata.forEach((key, value) ->
                copy.put(requireText(key, "metadata key"), value)
        );
        return Map.copyOf(copy);
    }

    private static List<LogicalTestResult> immutableTests(
            List<LogicalTestResult> tests
    ) {
        if (tests == null || tests.isEmpty()) {
            return List.of();
        }

        for (LogicalTestResult test : tests) {
            Objects.requireNonNull(test, "tests contains null");
        }
        return List.copyOf(tests);
    }

    private static List<ExecutionProblem> immutableProblems(
            List<ExecutionProblem> executionProblems
    ) {
        if (executionProblems == null || executionProblems.isEmpty()) {
            return List.of();
        }

        for (ExecutionProblem problem : executionProblems) {
            Objects.requireNonNull(problem, "executionProblems contains null");
        }
        return List.copyOf(executionProblems);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
