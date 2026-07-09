package com.bavykin.qualistrata.ui.driver;

import com.bavykin.qualistrata.ui.UiAutomationException;
import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.playwright.PlaywrightUiDriverFactory;
import com.bavykin.qualistrata.ui.selenium.SeleniumUiDriverFactory;

/**
 * Engine-switching driver factory used by consumer projects.
 */
public final class DefaultUiDriverFactory implements UiDriverFactory {

    private final UiDriverFactory playwrightFactory;
    private final UiDriverFactory seleniumFactory;

    /** Creates factory with default engine implementations. */
    public DefaultUiDriverFactory() {
        this(new PlaywrightUiDriverFactory(), new SeleniumUiDriverFactory());
    }

    /** Creates factory with explicit engine implementations, useful for tests. */
    public DefaultUiDriverFactory(UiDriverFactory playwrightFactory, UiDriverFactory seleniumFactory) {
        this.playwrightFactory = require(playwrightFactory, "Playwright UI driver factory");
        this.seleniumFactory = require(seleniumFactory, "Selenium UI driver factory");
    }

    @Override
    public UiDriver create(UiRuntimeConfig config) {
        if (config == null) {
            throw new UiAutomationException("UI runtime config must not be null.");
        }

        return switch (config.engine()) {
            case PLAYWRIGHT -> playwrightFactory.create(config);
            case SELENIUM -> seleniumFactory.create(config);
        };
    }

    private UiDriverFactory require(UiDriverFactory factory, String name) {
        if (factory == null) {
            throw new UiAutomationException(name + " must not be null.");
        }

        return factory;
    }
}
