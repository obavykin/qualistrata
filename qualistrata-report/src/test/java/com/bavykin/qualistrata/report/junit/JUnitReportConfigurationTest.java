package com.bavykin.qualistrata.report.junit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnitReportConfigurationTest {

    @Test
    void createsDefaultEnabledConfiguration() {
        JUnitReportConfiguration configuration =
                JUnitReportConfiguration.fromProperties(
                        new Properties(),
                        Path.of("workspace")
                );

        assertTrue(configuration.enabled());
        assertEquals("test", configuration.source());
        assertFalse(configuration.runId().isBlank());
        assertEquals(
                Path.of(
                        "workspace",
                        "build",
                        "reports",
                        "qualistrata",
                        "test",
                        "test-run.json"
                ),
                configuration.outputPath()
        );
    }

    @Test
    void appliesExplicitPropertiesAndSanitizesDefaultDirectory() {
        Properties properties = new Properties();
        properties.setProperty(
                JUnitReportConfiguration.ENABLED_PROPERTY,
                "false"
        );
        properties.setProperty(
                JUnitReportConfiguration.RUN_ID_PROPERTY,
                "run-123"
        );
        properties.setProperty(
                JUnitReportConfiguration.SOURCE_PROPERTY,
                "UI smoke test"
        );

        JUnitReportConfiguration configuration =
                JUnitReportConfiguration.fromProperties(
                        properties,
                        Path.of("workspace")
                );

        assertFalse(configuration.enabled());
        assertEquals("run-123", configuration.runId());
        assertEquals("UI smoke test", configuration.source());
        assertEquals(
                Path.of(
                        "workspace",
                        "build",
                        "reports",
                        "qualistrata",
                        "UI_smoke_test",
                        "test-run.json"
                ),
                configuration.outputPath()
        );
    }

    @Test
    void resolvesExplicitRelativeOutputAgainstWorkingDirectory() {
        Properties properties = new Properties();
        properties.setProperty(
                JUnitReportConfiguration.OUTPUT_PROPERTY,
                "custom/report.json"
        );

        JUnitReportConfiguration configuration =
                JUnitReportConfiguration.fromProperties(
                        properties,
                        Path.of("workspace")
                );

        assertEquals(
                Path.of("workspace", "custom", "report.json"),
                configuration.outputPath()
        );
    }
}
