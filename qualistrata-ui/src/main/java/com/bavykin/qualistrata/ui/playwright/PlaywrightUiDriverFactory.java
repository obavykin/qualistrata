package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.UiAutomationException;
import com.bavykin.qualistrata.ui.config.UiBrowserType;
import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.driver.UiDriver;
import com.bavykin.qualistrata.ui.driver.UiDriverFactory;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

/**
 * Creates Playwright-based UI drivers.
 */
public final class PlaywrightUiDriverFactory implements UiDriverFactory {

    /**
     * Creates a Playwright UI driver factory.
     */
    public PlaywrightUiDriverFactory() {
    }

    @Override
    public UiDriver create(UiRuntimeConfig config) {
        if (config == null) {
            throw new UiAutomationException("UI runtime config must not be null.");
        }

        Playwright playwright = Playwright.create();
        Browser browser = browserType(playwright, config.browser())
                .launch(new BrowserType.LaunchOptions().setHeadless(config.headless()));
        return new PlaywrightUiDriver(playwright, browser, config);
    }

    private BrowserType browserType(Playwright playwright, UiBrowserType type) {
        return switch (type) {
            case CHROMIUM, CHROME, EDGE -> playwright.chromium();
            case FIREFOX -> playwright.firefox();
            case WEBKIT -> playwright.webkit();
        };
    }
}
