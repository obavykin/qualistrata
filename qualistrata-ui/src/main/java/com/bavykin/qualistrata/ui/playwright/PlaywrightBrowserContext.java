package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.driver.UiBrowserContext;
import com.bavykin.qualistrata.ui.driver.UiCapabilities;
import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.driver.UiSessionRegistry;
import com.bavykin.qualistrata.ui.config.UiTraceMode;
import com.microsoft.playwright.BrowserContext;

/**
 * Playwright browser context wrapper.
 */
public final class PlaywrightBrowserContext implements UiBrowserContext {

    private final BrowserContext context;
    private final UiRuntimeConfig config;
    private final UiCapabilities capabilities;

    /** Creates wrapper around native Playwright context. */
    public PlaywrightBrowserContext(BrowserContext context, UiRuntimeConfig config, UiCapabilities capabilities) {
        this.context = context;
        this.config = config;
        this.capabilities = capabilities;
        this.context.setDefaultTimeout(config.timeouts().defaultTimeout().toMillis());
        if (config.traceMode() == UiTraceMode.ON_FAILURE) {
            this.context.tracing().start(new com.microsoft.playwright.Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true));
        }
    }

    @Override
    public UiSession newSession() {
        return UiSessionRegistry.register(new PlaywrightUiSession(context.newPage(), config, capabilities));
    }

    @Override
    public void close() {
        context.close();
    }
}
