package com.bavykin.qualistrata.ui.testng;

import com.bavykin.qualistrata.ui.artifact.NoOpUiArtifactCollector;
import com.bavykin.qualistrata.ui.config.BrowserEngine;
import com.bavykin.qualistrata.ui.config.UiBrowserType;
import com.bavykin.qualistrata.ui.reporting.UiRunReport;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiTestLifecycleListenerTest {

    @Test
    void buildsEmptyRunReportFromRuntimeProperties() {
        Properties properties = new Properties();
        properties.setProperty("target", "release_6.6");
        properties.setProperty("ui.engine", "playwright");
        properties.setProperty("ui.browser", "firefox");
        properties.setProperty("ui.headless", "false");

        UiTestLifecycleListener listener = new UiTestLifecycleListener(
                properties,
                Path.of("build/reports/ui-test"),
                NoOpUiArtifactCollector.INSTANCE,
                List.of()
        );

        UiRunReport report = listener.buildReport();

        assertEquals("release_6.6", report.target());
        assertEquals(BrowserEngine.PLAYWRIGHT, report.engine());
        assertEquals(UiBrowserType.FIREFOX, report.browser());
        assertEquals(0, report.totalTests());
        assertTrue(report.duration().toMillis() >= 0);
    }
}
