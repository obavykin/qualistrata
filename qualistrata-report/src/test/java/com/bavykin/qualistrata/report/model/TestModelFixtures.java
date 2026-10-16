package com.bavykin.qualistrata.report.model;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

final class TestModelFixtures {

    private static final Instant BASE =
            Instant.parse("2026-07-17T08:00:00Z");

    private TestModelFixtures() {
    }

    static TestDescriptor descriptor() {
        return descriptor("test-id");
    }

    static TestDescriptor descriptor(String uniqueId) {
        return new TestDescriptor(
                new TestIdentifier(
                        uniqueId,
                        TestFramework.JUNIT,
                        "junit-jupiter",
                        "regressionTest",
                        "example.ReportTest",
                        "monthlyReport",
                        null
                ),
                "Monthly report",
                Map.of("month", "200107"),
                Set.of("regression")
        );
    }

    static TestExecutionResult passedResult(long offsetMillis) {
        return result(
                descriptor(),
                TestStatus.PASSED,
                offsetMillis,
                offsetMillis + 100,
                null
        );
    }

    static TestExecutionResult failedResult() {
        return result(
                descriptor(),
                TestStatus.FAILED,
                0,
                100,
                new FailureDetails(
                        "java.lang.AssertionError",
                        "failed",
                        null
                )
        );
    }

    static TestExecutionResult result(
            TestDescriptor descriptor,
            TestStatus status,
            long startOffsetMillis,
            long finishOffsetMillis,
            FailureDetails failure
    ) {
        return new TestExecutionResult(
                descriptor,
                status,
                BASE.plusMillis(startOffsetMillis),
                BASE.plusMillis(finishOffsetMillis),
                failure
        );
    }
}
