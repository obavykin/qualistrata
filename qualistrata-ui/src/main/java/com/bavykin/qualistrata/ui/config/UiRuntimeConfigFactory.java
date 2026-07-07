package com.bavykin.qualistrata.ui.config;

import com.bavykin.qualistrata.core.config.resolved.ResolvedUiConfig;

import java.time.Duration;
import java.util.Properties;

/**
 * Creates {@link UiRuntimeConfig} from resolved core UI resources and runtime overrides.
 *
 * <p>The factory is the boundary between {@code qualistrata-core} target
 * resolution and {@code qualistrata-ui} browser execution. It intentionally
 * accepts a {@link Properties} object so tests and Gradle/Jenkins integrations
 * can pass only the already-approved runtime properties.</p>
 */
public final class UiRuntimeConfigFactory {

    private UiRuntimeConfigFactory() {
    }

    /**
     * Creates configuration from core UI resource and overrides.
     *
     * @param resolvedUi resolved core UI config.
     * @param properties runtime overrides, usually system properties.
     * @return runtime UI config.
     */
    public static UiRuntimeConfig from(ResolvedUiConfig resolvedUi, Properties properties) {
        if (resolvedUi == null) {
            throw new IllegalArgumentException("Resolved UI config must not be null.");
        }

        String baseUrl = override(properties, "ui.baseUrl", resolvedUi.baseUrl());
        UiTimeouts defaultTimeouts = UiTimeouts.DEFAULT;
        UiTimeouts timeouts = new UiTimeouts(
                duration(properties, "ui.timeout.ms", defaultTimeouts.defaultTimeout()),
                duration(properties, "ui.navigation.timeout.ms", defaultTimeouts.navigationTimeout()),
                duration(properties, "ui.newWindow.timeout.ms", defaultTimeouts.newWindowTimeout()),
                duration(properties, "ui.download.timeout.ms", defaultTimeouts.downloadTimeout())
        );

        return new UiRuntimeConfig(
                baseUrl,
                BrowserEngine.from(override(properties, "ui.engine", null)),
                UiBrowserType.from(override(properties, "ui.browser", null)),
                UiExecutionMode.valueOf(override(properties, "ui.execution.mode", "LOCAL").toUpperCase()),
                bool(properties, "ui.headless", true),
                timeouts,
                bool(properties, "ui.screenshot.on.failure", true),
                UiTraceMode.from(override(properties, "ui.trace.mode", null)),
                bool(properties, "ui.video.on.failure", false),
                integer(properties, "ui.max.parallel.sessions", 1)
        );
    }

    private static String override(Properties properties, String key, String fallback) {
        if (properties == null) {
            return fallback;
        }

        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static boolean bool(Properties properties, String key, boolean fallback) {
        String value = override(properties, key, null);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static int integer(Properties properties, String key, int fallback) {
        String value = override(properties, key, null);
        return value == null ? fallback : Integer.parseInt(value);
    }

    private static Duration duration(Properties properties, String key, Duration fallback) {
        String value = override(properties, key, null);
        return value == null ? fallback : Duration.ofMillis(Long.parseLong(value));
    }
}
