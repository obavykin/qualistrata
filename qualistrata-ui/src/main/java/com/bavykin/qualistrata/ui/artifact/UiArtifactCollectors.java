package com.bavykin.qualistrata.ui.artifact;

import com.bavykin.qualistrata.ui.config.UiTraceMode;
import com.bavykin.qualistrata.ui.playwright.PlaywrightScreenshotArtifactCollector;
import com.bavykin.qualistrata.ui.playwright.PlaywrightTraceArtifactCollector;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Factory methods for commonly used UI artifact collectors.
 */
public final class UiArtifactCollectors {

    private UiArtifactCollectors() {
    }

    /**
     * Creates default local failure collectors from runtime properties.
     *
     * @param outputDirectory UI report output directory.
     * @param properties runtime properties, usually system properties.
     * @return collector suitable for TestNG lifecycle integration.
     */
    public static UiArtifactCollector localFailureCollectors(Path outputDirectory, Properties properties) {
        List<UiArtifactCollector> collectors = new ArrayList<>();
        if (bool(properties, "ui.screenshot.on.failure", true)) {
            collectors.add(new PlaywrightScreenshotArtifactCollector(outputDirectory.resolve("artifacts/screenshots")));
        }

        UiTraceMode traceMode = UiTraceMode.from(value(properties, "ui.trace.mode"));
        if (traceMode == UiTraceMode.ON_FAILURE) {
            collectors.add(new PlaywrightTraceArtifactCollector(outputDirectory.resolve("artifacts/traces")));
        }

        return collectors.isEmpty() ? NoOpUiArtifactCollector.INSTANCE : new CompositeUiArtifactCollector(collectors);
    }

    private static boolean bool(Properties properties, String key, boolean fallback) {
        String value = value(properties, key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static String value(Properties properties, String key) {
        if (properties == null) {
            return null;
        }
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? null : value;
    }
}
