package com.bavykin.qualistrata.ui.driver;

import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;

/**
 * Creates {@link UiDriver} instances from runtime configuration.
 */
public interface UiDriverFactory {

    /**
     * Creates a driver.
     *
     * @param config runtime UI configuration.
     * @return created driver.
     */
    UiDriver create(UiRuntimeConfig config);
}
