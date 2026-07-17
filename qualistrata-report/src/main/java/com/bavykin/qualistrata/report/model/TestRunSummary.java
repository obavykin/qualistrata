package com.bavykin.qualistrata.report.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable status counts calculated from logical test results.
 *
 * @param total total number of logical tests
 * @param passed number of final passed results
 * @param failed number of final failed results
 * @param skipped number of final skipped results
 * @param aborted number of final aborted results
 * @param retried number of logical tests with more than one attempt
 */
public record TestRunSummary(
        int total,
        int passed,
        int failed,
        int skipped,
        int aborted,
        int retried
) {

    /**
     * Creates and validates summary counters.
     */
    public TestRunSummary {
        if (total < 0 || passed < 0 || failed < 0
                || skipped < 0 || aborted < 0 || retried < 0) {
            throw new IllegalArgumentException(
                    "Test-run summary counters must not be negative"
            );
        }

        if (passed + failed + skipped + aborted != total) {
            throw new IllegalArgumentException(
                    "Status counters must add up to total"
            );
        }

        if (retried > total) {
            throw new IllegalArgumentException(
                    "Retried count must not exceed total"
            );
        }
    }

    /**
     * Calculates a summary from immutable logical test results.
     *
     * @param tests logical test results
     * @return calculated summary
     */
    public static TestRunSummary from(List<LogicalTestResult> tests) {
        if (tests == null) {
            throw new NullPointerException("tests");
        }

        Map<TestStatus, Integer> counts = new EnumMap<>(TestStatus.class);
        int retried = 0;

        for (LogicalTestResult test : tests) {
            if (test == null) {
                throw new NullPointerException("tests contains null");
            }
            counts.merge(test.finalStatus(), 1, Integer::sum);
            if (test.retried()) {
                retried++;
            }
        }

        return new TestRunSummary(
                tests.size(),
                counts.getOrDefault(TestStatus.PASSED, 0),
                counts.getOrDefault(TestStatus.FAILED, 0),
                counts.getOrDefault(TestStatus.SKIPPED, 0),
                counts.getOrDefault(TestStatus.ABORTED, 0),
                retried
        );
    }
}
