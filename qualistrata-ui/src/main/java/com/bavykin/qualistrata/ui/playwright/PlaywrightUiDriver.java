package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.driver.UiBrowserContext;
import com.bavykin.qualistrata.ui.driver.UiCapabilities;
import com.bavykin.qualistrata.ui.driver.UiDriver;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

/**
 * Playwright browser-engine runtime owner.
 */
public final class PlaywrightUiDriver implements UiDriver {

    private final Playwright playwright;
    private final Browser browser;
    private final UiRuntimeConfig config;

    /**
     * Creates driver from already launched Playwright runtime and browser.
     *
     * @param playwright native Playwright runtime.
     * @param browser native Playwright browser.
     * @param config effective UI runtime configuration.
     */
    public PlaywrightUiDriver(Playwright playwright, Browser browser, UiRuntimeConfig config) {
        this.playwright = playwright;
        this.browser = browser;
        this.config = config;
    }

    @Override
    public UiBrowserContext newContext() {
        Browser.NewContextOptions options = new Browser.NewContextOptions();
        return new PlaywrightBrowserContext(browser.newContext(options), config, capabilities());
    }

    @Override
    public UiCapabilities capabilities() {
        return UiCapabilities.PLAYWRIGHT;
    }

    @Override
    public void close() {
        browser.close();
        playwright.close();
    }
}
