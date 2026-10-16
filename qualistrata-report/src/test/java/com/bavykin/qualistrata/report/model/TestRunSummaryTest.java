package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestRunSummaryTest {

    @Test
    void calculatesAllStatusAndRetryCounters() {
        TestRunSummary summary = TestRunSummary.from(List.of(
                logical(TestStatus.PASSED, false, "passed"),
                logical(TestStatus.FAILED, false, "failed"),
                logical(TestStatus.SKIPPED, false, "skipped"),
                logical(TestStatus.ABORTED, false, "aborted"),
                logical(TestStatus.PASSED, true, "retried")
        ));

        assertEquals(5, summary.total());
        assertEquals(2, summary.passed());
        assertEquals(1, summary.failed());
        assertEquals(1, summary.skipped());
        assertEquals(1, summary.aborted());
        assertEquals(1, summary.retried());
    }

    @Test
    void rejectsInconsistentCounters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TestRunSummary(2, 1, 0, 0, 0, 0)
        );
    }

    private static LogicalTestResult logical(
            TestStatus status,
            boolean retried,
            String uniqueId
    ) {
        TestDescriptor descriptor = TestModelFixtures.descriptor(uniqueId);
        TestExecutionResult first = result(descriptor, status, 0);

        if (!retried) {
            return LogicalTestResult.of(
                    descriptor,
                    List.of(new TestAttempt(1, first))
            );
        }

        return LogicalTestResult.of(
                descriptor,
                List.of(
                        new TestAttempt(
                                1,
                                result(descriptor, TestStatus.FAILED, 0)
                        ),
                        new TestAttempt(
                                2,
                                result(descriptor, status, 100)
                        )
                )
        );
    }

    private static TestExecutionResult result(
            TestDescriptor descriptor,
            TestStatus status,
            long offset
    ) {
        FailureDetails failure = status == TestStatus.FAILED
                ? new FailureDetails(null, "failed", null)
                : null;

        return TestModelFixtures.result(
                descriptor,
                status,
                offset,
                offset + 100,
                failure
        );
    }
}
