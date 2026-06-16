package com.bavykin.qualistrata.core.environment;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link EnvironmentCheckRunner}.
 */
class EnvironmentCheckRunnerTest {

    @Test
    void shouldRunEnvironmentCheckDefinitions() {
        EnvironmentCheckRunner runner = new EnvironmentCheckRunner();

        EnvironmentCheckSuiteResult suite = runner.run(
                "Target readiness",
                List.of(
                        EnvironmentCheckDefinition.error(
                                "unix.app.connectivity",
                                "Unix app connectivity",
                                () -> EnvironmentCheckResult.passed(
                                        "unix.app.connectivity",
                                        "Unix app connectivity",
                                        "Unix app host is reachable."
                                )
                        ),
                        EnvironmentCheckDefinition.warning(
                                "ui.base-url",
                                "UI base URL",
                                () -> EnvironmentCheckResult.skipped(
                                        "ui.base-url",
                                        "UI base URL",
                                        EnvironmentCheckSeverity.WARNING,
                                        "UI base URL is not configured."
                                )
                        )
                )
        );

        assertEquals("Target readiness", suite.name());
        assertEquals(2, suite.results().size());
        assertEquals(1, suite.passedCount());
        assertEquals(0, suite.failedCount());
        assertEquals(1, suite.skippedCount());
        assertFalse(suite.hasFailures());
        assertFalse(suite.hasBlockingFailures());
        assertNotNull(suite.duration());
    }

    @Test
    void shouldAddMeasuredDurationWhenCheckDoesNotProvideDuration() {
        EnvironmentCheckRunner runner = new EnvironmentCheckRunner();

        EnvironmentCheckSuiteResult suite = runner.run(
                "Target readiness",
                List.of(
                        EnvironmentCheckDefinition.error(
                                "unix.app.connectivity",
                                "Unix app connectivity",
                                () -> EnvironmentCheckResult.passed(
                                        "unix.app.connectivity",
                                        "Unix app connectivity",
                                        "Unix app host is reachable."
                                )
                        )
                )
        );

        assertNotNull(suite.results().getFirst().duration());
        assertFalse(suite.results().getFirst().duration().isNegative());
    }

    @Test
    void shouldKeepProvidedCheckDuration() {
        EnvironmentCheckRunner runner = new EnvironmentCheckRunner();
        Duration providedDuration = Duration.ofMillis(250);

        EnvironmentCheckSuiteResult suite = runner.run(
                "Target readiness",
                List.of(
                        EnvironmentCheckDefinition.error(
                                "unix.app.connectivity",
                                "Unix app connectivity",
                                () -> EnvironmentCheckResult.passed(
                                        "unix.app.connectivity",
                                        "Unix app connectivity",
                                        "Unix app host is reachable.",
                                        providedDuration,
                                        java.util.Map.of()
                                )
                        )
                )
        );

        assertEquals(providedDuration, suite.results().getFirst().duration());
    }

    @Test
    void shouldConvertNullResultToFailedResult() {
        EnvironmentCheckRunner runner = new EnvironmentCheckRunner();

        EnvironmentCheckSuiteResult suite = runner.run(
                "Target readiness",
                List.of(
                        EnvironmentCheckDefinition.error(
                                "broken.check",
                                "Broken check",
                                () -> null
                        )
                )
        );

        EnvironmentCheckResult result = suite.results().getFirst();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(EnvironmentCheckSeverity.ERROR, result.severity());
        assertEquals("Environment check returned null result.", result.message());
        assertTrue(result.isBlockingFailure());
    }

    @Test
    void shouldConvertRuntimeExceptionToFailedResult() {
        EnvironmentCheckRunner runner = new EnvironmentCheckRunner();
        RuntimeException error = new RuntimeException("Connection refused.");

        EnvironmentCheckSuiteResult suite = runner.run(
                "Target readiness",
                List.of(
                        EnvironmentCheckDefinition.warning(
                                "unix.app.connectivity",
                                "Unix app connectivity",
                                () -> {
                                    throw error;
                                }
                        )
                )
        );

        EnvironmentCheckResult result = suite.results().getFirst();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(EnvironmentCheckSeverity.WARNING, result.severity());
        assertEquals(
                "Environment check failed with unexpected exception: Connection refused.",
                result.message()
        );
        assertEquals(RuntimeException.class.getName(), result.details().get("exceptionType"));
        assertSame(error, result.error());
        assertFalse(result.isBlockingFailure());
    }

    @Test
    void shouldRunEmptySuite() {
        EnvironmentCheckRunner runner = new EnvironmentCheckRunner();

        EnvironmentCheckSuiteResult suite = runner.run(
                "Target readiness",
                List.of()
        );

        assertEquals("Target readiness", suite.name());
        assertTrue(suite.results().isEmpty());
        assertFalse(suite.allPassed());
        assertFalse(suite.hasFailures());
        assertFalse(suite.hasBlockingFailures());
    }

    @Test
    void shouldTreatNullDefinitionsAsEmptySuite() {
        EnvironmentCheckRunner runner = new EnvironmentCheckRunner();

        EnvironmentCheckSuiteResult suite = runner.run(
                "Target readiness",
                null
        );

        assertEquals("Target readiness", suite.name());
        assertTrue(suite.results().isEmpty());
    }

    @Test
    void shouldFailWhenSuiteNameIsBlank() {
        EnvironmentCheckRunner runner = new EnvironmentCheckRunner();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> runner.run(" ", List.of())
        );

        assertEquals("Environment check suite name must not be blank.", exception.getMessage());
    }
}