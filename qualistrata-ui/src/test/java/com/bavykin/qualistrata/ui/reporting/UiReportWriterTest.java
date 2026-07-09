package com.bavykin.qualistrata.ui.reporting;

import com.bavykin.qualistrata.ui.config.BrowserEngine;
import com.bavykin.qualistrata.ui.config.UiBrowserType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for local UI report writers.
 */
class UiReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteJsonAndHtmlReports() {
        UiRunReport report = new UiRunReport(
                "run-1",
                "target-a",
                BrowserEngine.PLAYWRIGHT,
                UiBrowserType.CHROMIUM,
                true,
                Instant.now(),
                Duration.ofMillis(100),
                List.of()
        );

        new JsonUiReportWriter().write(report, tempDir);
        new HtmlUiReportWriter().write(report, tempDir);

        assertTrue(Files.exists(tempDir.resolve("ui-report.json")));
        assertTrue(Files.exists(tempDir.resolve("ui-report.html")));
    }
}
