package com.bavykin.qualistrata.core.environment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link EnvironmentCheckDefinition}.
 */
class EnvironmentCheckDefinitionTest {

    @Test
    void shouldCreateErrorDefinition() {
        EnvironmentCheckDefinition definition = EnvironmentCheckDefinition.error(
                "unix.app.connectivity",
                "Unix app connectivity",
                () -> EnvironmentCheckResult.passed(
                        "unix.app.connectivity",
                        "Unix app connectivity",
                        "Unix app host is reachable."
                )
        );

        assertEquals("unix.app.connectivity", definition.id());
        assertEquals("Unix app connectivity", definition.name());
        assertEquals(EnvironmentCheckSeverity.ERROR, definition.severity());
    }

    @Test
    void shouldCreateWarningDefinition() {
        EnvironmentCheckDefinition definition = EnvironmentCheckDefinition.warning(
                "disk.space",
                "Disk space",
                () -> EnvironmentCheckResult.passed(
                        "disk.space",
                        "Disk space",
                        "Disk space is sufficient."
                )
        );

        assertEquals(EnvironmentCheckSeverity.WARNING, definition.severity());
    }

    @Test
    void shouldCreateInfoDefinition() {
        EnvironmentCheckDefinition definition = EnvironmentCheckDefinition.info(
                "metadata",
                "Metadata",
                () -> EnvironmentCheckResult.passed(
                        "metadata",
                        "Metadata",
                        "Metadata is available."
                )
        );

        assertEquals(EnvironmentCheckSeverity.INFO, definition.severity());
    }

    @Test
    void shouldFailWhenIdIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EnvironmentCheckDefinition.error(
                        " ",
                        "Unix app connectivity",
                        () -> EnvironmentCheckResult.passed(
                                "unix.app.connectivity",
                                "Unix app connectivity",
                                "Unix app host is reachable."
                        )
                )
        );

        assertEquals("Environment check definition id must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EnvironmentCheckDefinition.error(
                        "unix.app.connectivity",
                        " ",
                        () -> EnvironmentCheckResult.passed(
                                "unix.app.connectivity",
                                "Unix app connectivity",
                                "Unix app host is reachable."
                        )
                )
        );

        assertEquals("Environment check definition name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenSeverityIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new EnvironmentCheckDefinition(
                        "unix.app.connectivity",
                        "Unix app connectivity",
                        null,
                        () -> EnvironmentCheckResult.passed(
                                "unix.app.connectivity",
                                "Unix app connectivity",
                                "Unix app host is reachable."
                        )
                )
        );

        assertEquals("Environment check definition severity must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenCheckIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new EnvironmentCheckDefinition(
                        "unix.app.connectivity",
                        "Unix app connectivity",
                        EnvironmentCheckSeverity.ERROR,
                        null
                )
        );

        assertEquals("Environment check must not be null.", exception.getMessage());
    }
}