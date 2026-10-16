package com.bavykin.qualistrata.report.writer;

import com.bavykin.qualistrata.report.model.ExecutionProblem;
import com.bavykin.qualistrata.report.model.FailureDetails;
import com.bavykin.qualistrata.report.model.LogicalTestResult;
import com.bavykin.qualistrata.report.model.TestAttempt;
import com.bavykin.qualistrata.report.model.TestDescriptor;
import com.bavykin.qualistrata.report.model.TestExecutionResult;
import com.bavykin.qualistrata.report.model.TestFramework;
import com.bavykin.qualistrata.report.model.TestIdentifier;
import com.bavykin.qualistrata.report.model.TestRun;
import com.bavykin.qualistrata.report.model.TestRunDescriptor;
import com.bavykin.qualistrata.report.model.TestStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonTestRunWriterTest {

    private static final Instant START =
            Instant.parse("2026-07-17T08:00:00Z");

    private final JsonTestRunWriter writer = new JsonTestRunWriter();
    private final ObjectMapper reader = new ObjectMapper();

    @Test
    void writesSchemaVersionedDeterministicJson() throws Exception {
        String first = writer.toJson(testRun());
        String second = writer.toJson(testRun());

        assertEquals(first, second);

        JsonNode root = reader.readTree(first);
        assertEquals("1.0", root.path("schemaVersion").asText());
        assertEquals("run-1", root.path("runId").asText());
        assertEquals("regressionTest", root.path("source").asText());
        assertEquals(5000, root.path("durationMs").asLong());
        assertTrue(root.path("complete").asBoolean());

        assertEquals(1, root.path("summary").path("total").asInt());
        assertEquals(1, root.path("summary").path("passed").asInt());
        assertEquals(1, root.path("summary").path("retried").asInt());

        JsonNode test = root.path("tests").get(0);
        assertEquals("PASSED", test.path("finalStatus").asText());
        assertEquals(2, test.path("attempts").size());
        assertEquals(
                "FAILED",
                test.path("attempts").get(0).path("result").path("status").asText()
        );
        assertEquals(
                "PASSED",
                test.path("attempts").get(1).path("result").path("status").asText()
        );

        assertEquals(
                "beforeClass",
                root.path("executionProblems").get(0).path("phase").asText()
        );
    }

    @Test
    void writesThroughTemporarySiblingAndReplacesExistingFile(
            @TempDir Path directory
    ) throws Exception {
        Path output = directory.resolve("test-run.json");
        Files.writeString(output, "old");

        writer.write(testRun(), output);

        assertEquals(writer.toJson(testRun()), Files.readString(output));
        assertFalse(Files.exists(directory.resolve(".test-run.json.tmp")));
    }

    @Test
    void writesEmptyIncompleteRun() throws Exception {
        TestRun empty = TestRun.current(
                new TestRunDescriptor("empty", "test", START),
                START,
                false,
                Map.of(),
                List.of(),
                List.of()
        );

        JsonNode root = reader.readTree(writer.toJson(empty));

        assertFalse(root.path("complete").asBoolean());
        assertEquals(0, root.path("summary").path("total").asInt());
        assertTrue(root.path("tests").isEmpty());
        assertTrue(root.path("executionProblems").isEmpty());
    }

    private static TestRun testRun() {
        TestDescriptor descriptor = new TestDescriptor(
                new TestIdentifier(
                        "test-1",
                        TestFramework.JUNIT,
                        "junit-jupiter",
                        "regressionTest",
                        "example.ReportTest",
                        "monthlyReport",
                        "invocation-1"
                ),
                "Monthly report",
                Map.of("month", "200107"),
                Set.of("regression", "report")
        );

        FailureDetails attemptFailure = new FailureDetails(
                "java.lang.AssertionError",
                "first attempt failed",
                "stack"
        );

        TestExecutionResult failed = new TestExecutionResult(
                descriptor,
                TestStatus.FAILED,
                START,
                START.plusMillis(100),
                attemptFailure
        );
        TestExecutionResult passed = new TestExecutionResult(
                descriptor,
                TestStatus.PASSED,
                START.plusMillis(200),
                START.plusMillis(300),
                null
        );

        LogicalTestResult logicalTest = LogicalTestResult.of(
                descriptor,
                List.of(
                        new TestAttempt(1, failed),
                        new TestAttempt(2, passed)
                )
        );

        ExecutionProblem problem = new ExecutionProblem(
                "beforeClass",
                START.plusSeconds(1),
                new FailureDetails(
                        "java.lang.IllegalStateException",
                        "configuration failed",
                        null
                )
        );

        return TestRun.current(
                new TestRunDescriptor("run-1", "regressionTest", START),
                START.plusSeconds(5),
                true,
                Map.of(
                        "environment", "pt-b",
                        "application", "TNT"
                ),
                List.of(logicalTest),
                List.of(problem)
        );
    }
}
