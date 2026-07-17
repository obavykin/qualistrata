package com.bavykin.qualistrata.report.junit;

import com.bavykin.qualistrata.report.model.LogicalTestResult;
import com.bavykin.qualistrata.report.model.TestAttempt;
import com.bavykin.qualistrata.report.model.TestExecutionResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Groups captured JUnit executions into logical tests and ordered attempts.
 *
 * <p>Parameterized invocations remain separate because JUnit unique IDs include
 * invocation segments. Repeated executions with the same unique ID become
 * attempts of one logical test.</p>
 */
public final class JUnitResultAggregator {

    /**
     * Creates a JUnit result aggregator.
     */
    public JUnitResultAggregator() {
    }

    /**
     * Aggregates execution results by stable JUnit unique ID.
     *
     * @param executionResults raw captured JUnit execution results
     * @return deterministic immutable logical-test results
     */
    public List<LogicalTestResult> aggregate(
            List<TestExecutionResult> executionResults
    ) {
        Objects.requireNonNull(executionResults, "executionResults");

        List<TestExecutionResult> ordered = new ArrayList<>(executionResults);
        ordered.sort(Comparator
                .comparing(TestExecutionResult::startedAt)
                .thenComparing(result ->
                        result.test().identifier().uniqueId()
                )
                .thenComparing(TestExecutionResult::finishedAt));

        Map<String, List<TestExecutionResult>> grouped =
                new LinkedHashMap<>();
        for (TestExecutionResult result : ordered) {
            Objects.requireNonNull(
                    result,
                    "executionResults contains null"
            );
            grouped.computeIfAbsent(
                    result.test().identifier().uniqueId(),
                    ignored -> new ArrayList<>()
            ).add(result);
        }

        return grouped.values().stream()
                .map(JUnitResultAggregator::logicalResult)
                .sorted(Comparator.comparing(
                        result -> result.test().identifier().uniqueId()
                ))
                .toList();
    }

    private static LogicalTestResult logicalResult(
            List<TestExecutionResult> executions
    ) {
        List<TestAttempt> attempts = new ArrayList<>(executions.size());
        for (int index = 0; index < executions.size(); index++) {
            attempts.add(new TestAttempt(index + 1, executions.get(index)));
        }

        return LogicalTestResult.of(
                executions.getFirst().test(),
                attempts
        );
    }
}
