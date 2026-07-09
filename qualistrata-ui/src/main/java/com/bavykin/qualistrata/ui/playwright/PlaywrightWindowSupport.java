package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.driver.UiCapabilities;
import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.driver.UiSessionRegistry;
import com.bavykin.qualistrata.ui.driver.WindowSupport;
import com.microsoft.playwright.Page;

import java.time.Duration;

/**
 * Playwright popup/new-window helper.
 */
final class PlaywrightWindowSupport implements WindowSupport {

    private final Page page;
    private final UiRuntimeConfig config;
    private final UiCapabilities capabilities;

    PlaywrightWindowSupport(Page page, UiRuntimeConfig config, UiCapabilities capabilities) {
        this.page = page;
        this.config = config;
        this.capabilities = capabilities;
    }

    @Override
    public UiSession waitForNewWindow(Runnable action) {
        return waitForNewWindow(action, config.timeouts().newWindowTimeout());
    }

    @Override
    public UiSession waitForNewWindow(Runnable action, Duration timeout) {
        Page.WaitForPopupOptions options = new Page.WaitForPopupOptions()
                .setTimeout(timeout.toMillis());
        Page child = page.waitForPopup(options, action);
        return UiSessionRegistry.register(new PlaywrightUiSession(child, config, capabilities));
    }
}
