package com.bavykin.qualistrata.ui.selenium;

import com.bavykin.qualistrata.ui.UiAutomationException;
import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.driver.UiDriver;
import com.bavykin.qualistrata.ui.driver.UiDriverFactory;

/**
 * Selenium extension point.
 *
 * <p>The class makes Selenium support explicit in the public architecture while
 * preventing accidental use before a real Selenium adapter is implemented.</p>
 */
public final class SeleniumUiDriverFactory implements UiDriverFactory {

    @Override
    public UiDriver create(UiRuntimeConfig config) {
        throw new UiAutomationException(
                "Selenium UI adapter is not implemented yet. " +
                        "Use Playwright or provide a consumer-specific SeleniumUiDriverFactory."
        );
    }
}
