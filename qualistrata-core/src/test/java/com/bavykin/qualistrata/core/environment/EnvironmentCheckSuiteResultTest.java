package com.bavykin.qualistrata.core.environment;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link EnvironmentCheckSuiteResult}.
 */
class EnvironmentCheckSuiteResultTest {

    @Test
    void shouldCalculateSuiteSummary() {
        EnvironmentCheckResult passed = EnvironmentCheckResult.passed(
                "unix.app.connectivity",
                "Unix app connectivity",
                "Unix app host is reachable."
        );

        EnvironmentCheckResult failed = EnvironmentCheckResult.failed(
                "db.connectivity",
                "DB connectivity",
                EnvironmentCheckSeverity.ERROR,
                "DB is not reachable."
        );

        EnvironmentCheckResult skipped = EnvironmentCheckResult.skipped(
                "ui.base-url",
                "UI base URL",
                EnvironmentCheckSeverity.WARNING,
                "UI base URL is not configured."
        );

        EnvironmentCheckSuiteResult suite = new EnvironmentCheckSuiteResult(
                "Target readiness",
                List.of(passed, failed, skipped),
                Duration.ofMillis(300)
        );

        assertEquals("Target readiness", suite.name());
        assertEquals(3, suite.results().size());
        assertEquals(Duration.ofMillis(300), suite.duration());
        assertFalse(suite.allPassed());
        assertTrue(suite.hasFailures());
        assertTrue(suite.hasBlockingFailures());
        assertEquals(1, suite.passedCount());
        assertEquals(1, suite.failedCount());
        assertEquals(1, suite.skippedCount());
    }

    @Test
    void shouldReportAllPassedWhenAllResultsPassedAndSuiteIsNotEmpty() {
        EnvironmentCheckSuiteResult suite = new EnvironmentCheckSuiteResult(
                "Target readiness",
                List.of(
                        EnvironmentCheckResult.passed(
                                "unix.app.connectivity",
                                "Unix app connectivity",
                                "Unix app host is reachable."
                        )
                ),
                Duration.ofMillis(100)
        );

        assertTrue(suite.allPassed());
        assertFalse(suite.hasFailures());
        assertFalse(suite.hasBlockingFailures());
    }

    @Test
    void shouldNotReportAllPassedForEmptySuite() {
        EnvironmentCheckSuiteResult suite = new EnvironmentCheckSuiteResult(
                "Target readiness",
                List.of(),
                Duration.ZERO
        );

        assertFalse(suite.allPassed());
        assertFalse(suite.hasFailures());
        assertFalse(suite.hasBlockingFailures());
        assertEquals(0, suite.passedCount());
        assertEquals(0, suite.failedCount());
        assertEquals(0, suite.skippedCount());
    }

    @Test
    void shouldCopyResultsDefensively() {
        List<EnvironmentCheckResult> results = new java.util.ArrayList<>();
        results.add(EnvironmentCheckResult.passed(
                "unix.app.connectivity",
                "Unix app connectivity",
                "Unix app host is reachable."
        ));

        EnvironmentCheckSuiteResult suite = new EnvironmentCheckSuiteResult(
                "Target readiness",
                results,
                Duration.ofMillis(100)
        );

        results.clear();

        assertEquals(1, suite.results().size());
        assertThrows(UnsupportedOperationException.class, () -> suite.results().add(
                EnvironmentCheckResult.passed(
                        "extra",
                        "Extra check",
                        "Extra check passed."
                )
        ));
    }

    @Test
    void shouldFailWhenSuiteNameIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new EnvironmentCheckSuiteResult(
                        " ",
                        List.of(),
                        Duration.ZERO
                )
        );

        assertEquals("Environment check suite name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new EnvironmentCheckSuiteResult(
                        "Target readiness",
                        List.of(),
                        Duration.ofMillis(-1)
                )
        );

        assertEquals("Environment check suite duration must not be negative.", exception.getMessage());
    }
}