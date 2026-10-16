package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogicalTestResultTest {

    @Test
    void preservesFailedAttemptWhenRetryPasses() {
        FailureDetails firstFailure = new FailureDetails(
                "java.lang.AssertionError",
                "first attempt failed",
                null
        );
        TestExecutionResult failed = TestModelFixtures.result(
                TestModelFixtures.descriptor(),
                TestStatus.FAILED,
                0,
                100,
                firstFailure
        );
        TestExecutionResult passed = TestModelFixtures.passedResult(200);

        LogicalTestResult result = LogicalTestResult.of(
                TestModelFixtures.descriptor(),
                List.of(
                        new TestAttempt(1, failed),
                        new TestAttempt(2, passed)
                )
        );

        assertEquals(TestStatus.PASSED, result.finalStatus());
        assertTrue(result.retried());
        assertEquals(firstFailure, result.attempts().getFirst().result().failure());
        assertEquals(Duration.ofMillis(200), result.totalDuration());
    }

    @Test
    void reportsSingleExecutionAsNotRetried() {
        LogicalTestResult result = LogicalTestResult.of(
                TestModelFixtures.descriptor(),
                List.of(new TestAttempt(1, TestModelFixtures.passedResult(0)))
        );

        assertFalse(result.retried());
        assertEquals(TestStatus.PASSED, result.finalStatus());
    }

    @Test
    void defensivelyCopiesAttemptHistory() {
        List<TestAttempt> attempts = new ArrayList<>();
        attempts.add(new TestAttempt(1, TestModelFixtures.passedResult(0)));

        LogicalTestResult result = LogicalTestResult.of(
                TestModelFixtures.descriptor(),
                attempts
        );

        attempts.add(new TestAttempt(2, TestModelFixtures.passedResult(100)));

        assertEquals(1, result.attempts().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.attempts().add(
                        new TestAttempt(2, TestModelFixtures.passedResult(100))
                )
        );
    }

    @Test
    void rejectsNonContiguousAttemptNumbers() {
        assertThrows(
                IllegalArgumentException.class,
                () -> LogicalTestResult.of(
                        TestModelFixtures.descriptor(),
                        List.of(
                                new TestAttempt(
                                        1,
                                        TestModelFixtures.failedResult()
                                ),
                                new TestAttempt(
                                        3,
                                        TestModelFixtures.passedResult(100)
                                )
                        )
                )
        );
    }

    @Test
    void rejectsAttemptsFromAnotherTestIdentifier() {
        TestDescriptor anotherTest = TestModelFixtures.descriptor("another-id");

        assertThrows(
                IllegalArgumentException.class,
                () -> LogicalTestResult.of(
                        TestModelFixtures.descriptor(),
                        List.of(
                                new TestAttempt(
                                        1,
                                        TestModelFixtures.result(
                                                anotherTest,
                                                TestStatus.PASSED,
                                                0,
                                                100,
                                                null
                                        )
                                )
                        )
                )
        );
    }

    @Test
    void rejectsEmptyAttemptHistory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> LogicalTestResult.of(
                        TestModelFixtures.descriptor(),
                        List.of()
                )
        );
    }
}
