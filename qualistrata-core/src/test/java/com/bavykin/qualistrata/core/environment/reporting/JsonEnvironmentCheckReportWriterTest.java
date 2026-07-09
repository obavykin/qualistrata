package com.bavykin.qualistrata.core.environment.reporting;

import com.bavykin.qualistrata.core.environment.EnvironmentCheckResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSeverity;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSuiteResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link JsonEnvironmentCheckReportWriter}.
 */
class JsonEnvironmentCheckReportWriterTest {

    @TempDir
    private Path tempDir;

    @Test
    void shouldWriteJsonReport() throws Exception {
        Path outputFile = tempDir.resolve("reports/environment/report.json");

        new JsonEnvironmentCheckReportWriter().write(sampleSuite(), outputFile);

        String json = Files.readString(outputFile);

        assertTrue(Files.exists(outputFile));
        assertTrue(json.contains("\"name\": \"Target readiness\""));
        assertTrue(json.contains("\"passedCount\": 1"));
        assertTrue(json.contains("\"failedCount\": 1"));
        assertTrue(json.contains("\"skippedCount\": 0"));
        assertTrue(json.contains("\"id\": \"unix.app.hostname\""));
        assertTrue(json.contains("\"status\": \"PASSED\""));
        assertTrue(json.contains("\"severity\": \"ERROR\""));
        assertTrue(json.contains("\"hostAlias\": \"app\""));
        assertTrue(json.contains("\"error\": {"));
        assertTrue(json.contains("\"type\": \"java.lang.RuntimeException\""));
    }

    @Test
    void shouldEscapeJsonValues() throws Exception {
        Path outputFile = tempDir.resolve("report.json");

        EnvironmentCheckSuiteResult suite = new EnvironmentCheckSuiteResult(
                "Target \"readiness\"",
                List.of(
                        EnvironmentCheckResult.passed(
                                "check",
                                "Check",
                                "Line 1\nLine 2",
                                Duration.ofMillis(1),
                                Map.of("path", "C:\\temp\\file")
                        )
                ),
                Duration.ofMillis(1)
        );

        new JsonEnvironmentCheckReportWriter().write(suite, outputFile);

        String json = Files.readString(outputFile);

        assertTrue(json.contains("Target \\\"readiness\\\""));
        assertTrue(json.contains("Line 1\\nLine 2"));
        assertTrue(json.contains("C:\\\\temp\\\\file"));
    }

    @Test
    void shouldFailWhenSuiteIsNull() {
        EnvironmentCheckReportException exception = assertThrows(
                EnvironmentCheckReportException.class,
                () -> new JsonEnvironmentCheckReportWriter().write(null, tempDir.resolve("report.json"))
        );

        assertEquals("Environment check suite result must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenOutputFileIsNull() {
        EnvironmentCheckReportException exception = assertThrows(
                EnvironmentCheckReportException.class,
                () -> new JsonEnvironmentCheckReportWriter().write(sampleSuite(), null)
        );

        assertEquals("Environment check report output file must not be null.", exception.getMessage());
    }

    /**
     * Creates sample suite result.
     *
     * @return sample suite.
     */
    private EnvironmentCheckSuiteResult sampleSuite() {
        RuntimeException error = new RuntimeException("Connection refused.");

        return new EnvironmentCheckSuiteResult(
                "Target readiness",
                List.of(
                        EnvironmentCheckResult.passed(
                                "unix.app.hostname",
                                "Unix app hostname",
                                "Unix command environment check passed.",
                                Duration.ofMillis(10),
                                Map.of("hostAlias", "app")
                        ),
                        EnvironmentCheckResult.failed(
                                "unix.db.connectivity",
                                "Unix DB connectivity",
                                EnvironmentCheckSeverity.ERROR,
                                "Unix command failed.",
                                Duration.ofMillis(20),
                                Map.of("hostAlias", "db"),
                                error
                        )
                ),
                Duration.ofMillis(30)
        );
    }
}