package com.bavykin.qualistrata.ui.reporting;

import com.bavykin.qualistrata.ui.config.BrowserEngine;
import com.bavykin.qualistrata.ui.config.UiBrowserType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Aggregated UI run report.
 *
 * @param runId unique run id.
 * @param target selected target name or alias.
 * @param engine automation engine.
 * @param browser browser family.
 * @param headless whether run used headless browser.
 * @param startedAt run start timestamp.
 * @param duration run duration.
 * @param tests test reports.
 */
public record UiRunReport(
        String runId,
        String target,
        BrowserEngine engine,
        UiBrowserType browser,
        boolean headless,
        Instant startedAt,
        Duration duration,
        List<UiTestReport> tests
) {
    /** Creates report with immutable test list. */
    public UiRunReport {
        tests = tests == null ? List.of() : List.copyOf(tests);
    }

    /** @return total test count. */
    public long totalTests() {
        return tests.size();
    }

    /** @return passed test count. */
    public long passed() {
        return tests.stream().filter(test -> test.status() == UiTestStatus.PASSED).count();
    }

    /** @return failed test count. */
    public long failed() {
        return tests.stream().filter(test -> test.status() == UiTestStatus.FAILED).count();
    }

    /** @return skipped test count. */
    public long skipped() {
        return tests.stream().filter(test -> test.status() == UiTestStatus.SKIPPED).count();
    }
}
