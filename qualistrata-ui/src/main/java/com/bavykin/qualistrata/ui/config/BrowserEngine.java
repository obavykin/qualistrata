package com.bavykin.qualistrata.ui.config;

/**
 * Supported UI automation engines.
 *
 * <p>Playwright is the primary implementation. Selenium remains an explicit
 * architectural extension point so consumer tests can stay engine-independent
 * where features overlap.</p>
 */
public enum BrowserEngine {
    /** Playwright browser engine. */
    PLAYWRIGHT,
    /** Selenium browser engine. */
    SELENIUM;

    /**
     * Parses an engine name using a CI-friendly default.
     *
     * @param value optional engine name.
     * @return parsed engine or {@link #PLAYWRIGHT} when value is blank.
     */
    public static BrowserEngine from(String value) {
        if (value == null || value.isBlank()) {
            return PLAYWRIGHT;
        }

        return BrowserEngine.valueOf(value.trim().replace('-', '_').toUpperCase());
    }
}
