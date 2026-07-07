package com.bavykin.qualistrata.ui.config;

/**
 * Trace collection policy used by artifact collection integrations.
 */
public enum UiTraceMode {
    OFF,
    ON_FAILURE,
    ALWAYS;

    /**
     * Parses trace mode with failure-only tracing as a useful diagnostic default.
     *
     * @param value optional trace mode.
     * @return parsed mode.
     */
    public static UiTraceMode from(String value) {
        if (value == null || value.isBlank()) {
            return ON_FAILURE;
        }

        return UiTraceMode.valueOf(value.trim().replace('-', '_').toUpperCase());
    }
}
