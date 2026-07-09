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
 * Unit tests for {@link HtmlEnvironmentCheckReportWriter}.
 */
class HtmlEnvironmentCheckReportWriterTest {

    @TempDir
    private Path tempDir;

    @Test
    void shouldWriteHtmlReport() throws Exception {
        Path outputFile = tempDir.resolve("reports/environment/report.html");

        new HtmlEnvironmentCheckReportWriter().write(sampleSuite(), outputFile);

        String html = Files.readString(outputFile);

        assertTrue(Files.exists(outputFile));
        assertTrue(html.contains("<title>Environment Check Report</title>"));
        assertTrue(html.contains("Target readiness"));
        assertTrue(html.contains("unix.app.hostname"));
        assertTrue(html.contains("Unix app hostname"));
        assertTrue(html.contains("status-passed"));
        assertTrue(html.contains("status-failed"));
        assertTrue(html.contains("hostAlias=app"));
        assertTrue(html.contains("java.lang.RuntimeException: Connection refused."));
    }

    @Test
    void shouldEscapeHtmlValues() throws Exception {
        Path outputFile = tempDir.resolve("report.html");

        EnvironmentCheckSuiteResult suite = new EnvironmentCheckSuiteResult(
                "Target <readiness>",
                List.of(
                        EnvironmentCheckResult.passed(
                                "check",
                                "Check",
                                "Value <must> be \"escaped\"",
                                Duration.ofMillis(1),
                                Map.of("script", "<script>")
                        )
                ),
                Duration.ofMillis(1)
        );

        new HtmlEnvironmentCheckReportWriter().write(suite, outputFile);

        String html = Files.readString(outputFile);

        assertTrue(html.contains("Target &lt;readiness&gt;"));
        assertTrue(html.contains("Value &lt;must&gt; be &quot;escaped&quot;"));
        assertTrue(html.contains("script=&lt;script&gt;"));
    }

    @Test
    void shouldFailWhenSuiteIsNull() {
        EnvironmentCheckReportException exception = assertThrows(
                EnvironmentCheckReportException.class,
                () -> new HtmlEnvironmentCheckReportWriter().write(null, tempDir.resolve("report.html"))
        );

        assertEquals("Environment check suite result must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenOutputFileIsNull() {
        EnvironmentCheckReportException exception = assertThrows(
                EnvironmentCheckReportException.class,
                () -> new HtmlEnvironmentCheckReportWriter().write(sampleSuite(), null)
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