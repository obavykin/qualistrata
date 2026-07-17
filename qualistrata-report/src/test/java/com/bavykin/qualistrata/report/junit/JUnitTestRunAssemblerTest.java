package com.bavykin.qualistrata.report.junit;

import com.bavykin.qualistrata.report.metadata.TestRunMetadataCollector;
import com.bavykin.qualistrata.report.model.TestRun;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnitTestRunAssemblerTest {

    @Test
    void buildsCompleteRunWithCollectedMetadata() {
        JUnitTestRunListener listener = new JUnitTestRunListener();
        TestRunMetadataCollector metadataCollector =
                new TestRunMetadataCollector(List.of(
                        () -> Map.of(
                                "application", "TNT",
                                "environment", "pt-b"
                        )
                ));

        JUnitTestRunAssembler assembler =
                new JUnitTestRunAssembler(listener, metadataCollector);

        Instant startedAt = Instant.parse("2026-07-17T08:00:00Z");
        Instant finishedAt = Instant.parse("2026-07-17T08:01:00Z");

        TestRun testRun = assembler.complete(
                "run-123",
                "uiSmokeTest",
                startedAt,
                finishedAt
        );

        assertEquals("run-123", testRun.descriptor().runId());
        assertEquals("uiSmokeTest", testRun.descriptor().source());
        assertEquals(startedAt, testRun.descriptor().startedAt());
        assertEquals(finishedAt, testRun.finishedAt());
        assertEquals("TNT", testRun.metadata().get("application"));
        assertEquals("pt-b", testRun.metadata().get("environment"));
        assertTrue(testRun.complete());
        assertTrue(testRun.tests().isEmpty());
        assertTrue(testRun.executionProblems().isEmpty());
    }

    @Test
    void buildsPartialRunWhenNormalCompletionWasNotObserved() {
        JUnitTestRunAssembler assembler =
                new JUnitTestRunAssembler(
                        new JUnitTestRunListener(),
                        new TestRunMetadataCollector(List.of())
                );

        TestRun testRun = assembler.assemble(
                "run-partial",
                "regressionTest",
                Instant.parse("2026-07-17T08:00:00Z"),
                Instant.parse("2026-07-17T08:00:10Z"),
                false,
                List.of()
        );

        assertEquals("run-partial", testRun.descriptor().runId());
        assertTrue(testRun.metadata().isEmpty());
        assertTrue(testRun.tests().isEmpty());
        assertTrue(testRun.executionProblems().isEmpty());
        org.junit.jupiter.api.Assertions.assertFalse(testRun.complete());
    }
}
