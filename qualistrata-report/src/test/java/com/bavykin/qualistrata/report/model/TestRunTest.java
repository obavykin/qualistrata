package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestRunTest {

    private static final Instant START =
            Instant.parse("2026-07-17T08:00:00Z");

    @Test
    void calculatesDurationAndSummaryFromLogicalTests() {
        LogicalTestResult passed = LogicalTestResult.of(
                TestModelFixtures.descriptor("passed"),
                List.of(
                        new TestAttempt(
                                1,
                                TestModelFixtures.result(
                                        TestModelFixtures.descriptor("passed"),
                                        TestStatus.PASSED,
                                        0,
                                        100,
                                        null
                                )
                        )
                )
        );

        TestRun run = TestRun.current(
                new TestRunDescriptor("run-1", "regressionTest", START),
                START.plusSeconds(5),
                true,
                Map.of("application", "TNT"),
                List.of(passed),
                List.of()
        );

        assertEquals(TestRun.CURRENT_SCHEMA_VERSION, run.schemaVersion());
        assertEquals(Duration.ofSeconds(5), run.duration());
        assertEquals(1, run.summary().total());
        assertEquals(1, run.summary().passed());
    }

    @Test
    void defensivelyCopiesRunCollections() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("application", "TNT");
        List<LogicalTestResult> tests = new ArrayList<>();
        List<ExecutionProblem> problems = new ArrayList<>();

        TestRun run = TestRun.current(
                new TestRunDescriptor("run-1", "test", START),
                START.plusSeconds(1),
                false,
                metadata,
                tests,
                problems
        );

        metadata.put("environment", "pt-b");
        tests.add(null);
        problems.add(null);

        assertEquals(Map.of("application", "TNT"), run.metadata());
        assertEquals(List.of(), run.tests());
        assertEquals(List.of(), run.executionProblems());
        assertFalse(run.complete());

        assertThrows(
                UnsupportedOperationException.class,
                () -> run.metadata().put("environment", "pt-b")
        );
    }

    @Test
    void rejectsFinishBeforeStart() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TestRun.current(
                        new TestRunDescriptor("run-1", "test", START),
                        START.minusSeconds(1),
                        false,
                        Map.of(),
                        List.of(),
                        List.of()
                )
        );
    }
}
