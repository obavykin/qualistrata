package com.bavykin.qualistrata.ui.reporting;

import com.bavykin.qualistrata.ui.config.BrowserEngine;
import com.bavykin.qualistrata.ui.config.UiBrowserType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for aggregated UI report counters.
 */
class UiRunReportTest {

    @Test
    void shouldCalculateStatusCounters() {
        UiRunReport report = new UiRunReport(
                "run-1",
                "target-a",
                BrowserEngine.PLAYWRIGHT,
                UiBrowserType.CHROMIUM,
                true,
                Instant.now(),
                Duration.ofSeconds(1),
                List.of(test(UiTestStatus.PASSED), test(UiTestStatus.FAILED), test(UiTestStatus.SKIPPED))
        );

        assertEquals(3, report.totalTests());
        assertEquals(1, report.passed());
        assertEquals(1, report.failed());
        assertEquals(1, report.skipped());
    }

    private UiTestReport test(UiTestStatus status) {
        return new UiTestReport(
                "ExampleTest",
                "shouldWork",
                status,
                Instant.now(),
                Duration.ofMillis(10),
                List.of(),
                null,
                List.of(),
                List.of(),
                null
        );
    }
}
