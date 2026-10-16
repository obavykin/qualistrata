package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestExecutionResultTest {

    private static final Instant START = Instant.parse("2026-07-17T08:00:00Z");
    private static final Instant FINISH = START.plusMillis(1250);

    @Test
    void calculatesDurationAndExposesFailureAsOptional() {
        FailureDetails failure = new FailureDetails(
                "java.lang.AssertionError",
                "expected true",
                null
        );

        TestExecutionResult result = new TestExecutionResult(
                descriptor(),
                TestStatus.FAILED,
                START,
                FINISH,
                failure
        );

        assertEquals(Duration.ofMillis(1250), result.duration());
        assertEquals(failure, result.failureDetails().orElseThrow());
    }

    @Test
    void allowsPassedResultWithoutFailure() {
        TestExecutionResult result = new TestExecutionResult(
                descriptor(),
                TestStatus.PASSED,
                START,
                FINISH,
                null
        );

        assertTrue(result.failureDetails().isEmpty());
    }

    @Test
    void rejectsFinishBeforeStart() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TestExecutionResult(
                        descriptor(),
                        TestStatus.ABORTED,
                        FINISH,
                        START,
                        new FailureDetails(null, "aborted", null)
                )
        );
    }

    @Test
    void rejectsPassedResultWithFailure() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TestExecutionResult(
                        descriptor(),
                        TestStatus.PASSED,
                        START,
                        FINISH,
                        new FailureDetails(null, "unexpected", null)
                )
        );
    }

    @Test
    void rejectsFailedResultWithoutFailure() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TestExecutionResult(
                        descriptor(),
                        TestStatus.FAILED,
                        START,
                        FINISH,
                        null
                )
        );
    }

    private static TestDescriptor descriptor() {
        return new TestDescriptor(
                new TestIdentifier(
                        "test-id",
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
}
