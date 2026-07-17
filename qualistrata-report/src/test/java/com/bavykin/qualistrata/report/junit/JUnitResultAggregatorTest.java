package com.bavykin.qualistrata.report.junit;

import com.bavykin.qualistrata.report.model.FailureDetails;
import com.bavykin.qualistrata.report.model.LogicalTestResult;
import com.bavykin.qualistrata.report.model.TestDescriptor;
import com.bavykin.qualistrata.report.model.TestExecutionResult;
import com.bavykin.qualistrata.report.model.TestFramework;
import com.bavykin.qualistrata.report.model.TestIdentifier;
import com.bavykin.qualistrata.report.model.TestStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnitResultAggregatorTest {

    private static final Instant START =
            Instant.parse("2026-07-17T08:00:00Z");

    @Test
    void groupsRepeatedUniqueIdsAsOrderedAttempts() {
        TestDescriptor descriptor = descriptor("test-1");

        List<LogicalTestResult> logicalTests =
                new JUnitResultAggregator().aggregate(List.of(
                        result(
                                descriptor,
                                TestStatus.PASSED,
                                200,
                                300,
                                null
                        ),
                        result(
                                descriptor,
                                TestStatus.FAILED,
                                0,
                                100,
                                new FailureDetails(
                                        "java.lang.AssertionError",
                                        "failed",
                                        "stack"
                                )
                        )
                ));

        LogicalTestResult logicalTest = logicalTests.getFirst();

        assertEquals(1, logicalTests.size());
        assertEquals(2, logicalTest.attempts().size());
        assertEquals(
                TestStatus.FAILED,
                logicalTest.attempts().getFirst().result().status()
        );
        assertEquals(
                TestStatus.PASSED,
                logicalTest.attempts().getLast().result().status()
        );
        assertEquals(TestStatus.PASSED, logicalTest.finalStatus());
        assertTrue(logicalTest.retried());
    }

    @Test
    void keepsParameterizedInvocationsSeparateByUniqueId() {
        List<LogicalTestResult> logicalTests =
                new JUnitResultAggregator().aggregate(List.of(
                        result(
                                descriptor("invocation-2"),
                                TestStatus.PASSED,
                                100,
                                200,
                                null
                        ),
                        result(
                                descriptor("invocation-1"),
                                TestStatus.PASSED,
                                0,
                                100,
                                null
                        )
                ));

        assertEquals(2, logicalTests.size());
        assertEquals(
                "invocation-1",
                logicalTests.getFirst().test().identifier().uniqueId()
        );
        assertEquals(
                "invocation-2",
                logicalTests.getLast().test().identifier().uniqueId()
        );
        assertFalse(logicalTests.getFirst().retried());
    }

    private static TestDescriptor descriptor(String uniqueId) {
        return new TestDescriptor(
                new TestIdentifier(
                        uniqueId,
                        TestFramework.JUNIT,
                        "junit-jupiter",
                        null,
                        "example.ReportTest",
                        "reports",
                        uniqueId
                ),
                "reports",
                Map.of(),
                Set.of()
        );
    }

    private static TestExecutionResult result(
            TestDescriptor descriptor,
            TestStatus status,
            long startedAtOffset,
            long finishedAtOffset,
            FailureDetails failure
    ) {
        return new TestExecutionResult(
                descriptor,
                status,
                START.plusMillis(startedAtOffset),
                START.plusMillis(finishedAtOffset),
                failure
        );
    }
}
