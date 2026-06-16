package com.bavykin.qualistrata.core.environment;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link EnvironmentCheckResult}.
 */
class EnvironmentCheckResultTest {

    @Test
    void shouldCreatePassedResult() {
        EnvironmentCheckResult result = EnvironmentCheckResult.passed(
                "unix.app.connectivity",
                "Unix app connectivity",
                "Unix app host is reachable."
        );

        assertEquals("unix.app.connectivity", result.id());
        assertEquals("Unix app connectivity", result.name());
        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals(EnvironmentCheckSeverity.INFO, result.severity());
        assertEquals("Unix app host is reachable.", result.message());
        assertTrue(result.isPassed());
        assertFalse(result.isFailed());
        assertFalse(result.isSkipped());
        assertFalse(result.isBlockingFailure());
    }

    @Test
    void shouldCreateFailedResult() {
        RuntimeException error = new RuntimeException("Connection refused.");

        EnvironmentCheckResult result = EnvironmentCheckResult.failed(
                "unix.app.connectivity",
                "Unix app connectivity",
                EnvironmentCheckSeverity.ERROR,
                "Unix app host is not reachable.",
                Duration.ofMillis(120),
                Map.of("host", "app"),
                error
        );

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(EnvironmentCheckSeverity.ERROR, result.severity());
        assertEquals(Duration.ofMillis(120), result.duration());
        assertEquals("app", result.details().get("host"));
        assertEquals(error, result.error());
        assertTrue(result.isFailed());
        assertTrue(result.isBlockingFailure());
    }

    @Test
    void shouldCreateSkippedResult() {
        EnvironmentCheckResult result = EnvironmentCheckResult.skipped(
                "db.connectivity",
                "DB connectivity",
                EnvironmentCheckSeverity.WARNING,
                "DB configuration is not provided."
        );

        assertEquals(EnvironmentCheckStatus.SKIPPED, result.status());
        assertEquals(EnvironmentCheckSeverity.WARNING, result.severity());
        assertTrue(result.isSkipped());
        assertFalse(result.isBlockingFailure());
    }

    @Test
    void shouldCopyDetailsMapDefensively() {
        Map<String, String> details = new HashMap<>();
        details.put("host", "app");

        EnvironmentCheckResult result = EnvironmentCheckResult.passed(
                "unix.app.connectivity",
                "Unix app connectivity",
                "Unix app host is reachable.",
                Duration.ofMillis(10),
                details
        );

        details.put("host", "changed");

        assertEquals("app", result.details().get("host"));
        assertThrows(UnsupportedOperationException.class, () -> result.details().put("new", "value"));
    }

    @Test
    void shouldFailWhenIdIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EnvironmentCheckResult.passed(
                        " ",
                        "Unix app connectivity",
                        "Unix app host is reachable."
                )
        );

        assertEquals("Environment check id must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EnvironmentCheckResult.passed(
                        "unix.app.connectivity",
                        " ",
                        "Unix app host is reachable."
                )
        );

        assertEquals("Environment check name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenStatusIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new EnvironmentCheckResult(
                        "unix.app.connectivity",
                        "Unix app connectivity",
                        null,
                        EnvironmentCheckSeverity.INFO,
                        "Unix app host is reachable.",
                        null,
                        Map.of(),
                        null
                )
        );

        assertEquals("Environment check status must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenSeverityIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new EnvironmentCheckResult(
                        "unix.app.connectivity",
                        "Unix app connectivity",
                        EnvironmentCheckStatus.PASSED,
                        null,
                        "Unix app host is reachable.",
                        null,
                        Map.of(),
                        null
                )
        );

        assertEquals("Environment check severity must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenMessageIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EnvironmentCheckResult.passed(
                        "unix.app.connectivity",
                        "Unix app connectivity",
                        " "
                )
        );

        assertEquals("Environment check message must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EnvironmentCheckResult.passed(
                        "unix.app.connectivity",
                        "Unix app connectivity",
                        "Unix app host is reachable.",
                        Duration.ofMillis(-1),
                        Map.of()
                )
        );

        assertEquals("Environment check duration must not be negative.", exception.getMessage());
    }
}