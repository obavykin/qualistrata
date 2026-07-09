package com.bavykin.qualistrata.ui.config;

/**
 * Browser family requested by UI tests.
 */
public enum UiBrowserType {
    CHROMIUM,
    FIREFOX,
    WEBKIT,
    CHROME,
    EDGE;

    /**
     * Parses browser name using Chromium as the default CI-safe browser.
     *
     * @param value optional browser name.
     * @return parsed browser type or {@link #CHROMIUM} when value is blank.
     */
    public static UiBrowserType from(String value) {
        if (value == null || value.isBlank()) {
            return CHROMIUM;
        }

        return UiBrowserType.valueOf(value.trim().replace('-', '_').toUpperCase());
    }
}
