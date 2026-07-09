package com.bavykin.qualistrata.ui.config;

import com.bavykin.qualistrata.core.config.model.UiResourceConfig;
import com.bavykin.qualistrata.core.config.resolved.ResolvedUiConfig;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for resolving UI runtime config from core target data.
 */
class UiRuntimeConfigFactoryTest {

    @Test
    void shouldUseCoreBaseUrlAndDefaults() {
        UiRuntimeConfig config = UiRuntimeConfigFactory.from(resolved("https://example.test"), new Properties());

        assertEquals("https://example.test", config.baseUrl());
        assertEquals(BrowserEngine.PLAYWRIGHT, config.engine());
        assertEquals(UiBrowserType.CHROMIUM, config.browser());
        assertEquals(UiTraceMode.ON_FAILURE, config.traceMode());
    }

    @Test
    void shouldApplyRuntimeOverrides() {
        Properties properties = new Properties();
        properties.setProperty("ui.engine", "selenium");
        properties.setProperty("ui.browser", "firefox");
        properties.setProperty("ui.headless", "false");
        properties.setProperty("ui.timeout.ms", "15000");
        properties.setProperty("ui.max.parallel.sessions", "3");

        UiRuntimeConfig config = UiRuntimeConfigFactory.from(resolved("https://example.test"), properties);

        assertEquals(BrowserEngine.SELENIUM, config.engine());
        assertEquals(UiBrowserType.FIREFOX, config.browser());
        assertFalse(config.headless());
        assertEquals(15000, config.timeouts().defaultTimeout().toMillis());
        assertEquals(3, config.maxParallelSessions());
    }

    private ResolvedUiConfig resolved(String baseUrl) {
        return new ResolvedUiConfig(
                "main",
                "default",
                new UiResourceConfig("web", baseUrl, "default"),
                null,
                null,
                null
        );
    }
}
