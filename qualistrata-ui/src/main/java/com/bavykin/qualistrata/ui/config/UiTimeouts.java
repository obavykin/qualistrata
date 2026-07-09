package com.bavykin.qualistrata.ui.config;

import java.time.Duration;

/**
 * UI timeout bundle applied to browser sessions and waits.
 *
 * @param defaultTimeout regular element/action timeout.
 * @param navigationTimeout navigation and page-load timeout.
 * @param newWindowTimeout popup/new-tab timeout.
 * @param downloadTimeout download event timeout.
 */
public record UiTimeouts(
        Duration defaultTimeout,
        Duration navigationTimeout,
        Duration newWindowTimeout,
        Duration downloadTimeout
) {
    /** Default timeout bundle optimized for stable CI runs. */
    public static final UiTimeouts DEFAULT = new UiTimeouts(
            Duration.ofSeconds(30),
            Duration.ofSeconds(60),
            Duration.ofSeconds(30),
            Duration.ofSeconds(60)
    );

    /** Validates timeout values. */
    public UiTimeouts {
        defaultTimeout = positive(defaultTimeout, "Default UI timeout");
        navigationTimeout = positive(navigationTimeout, "Navigation UI timeout");
        newWindowTimeout = positive(newWindowTimeout, "New-window UI timeout");
        downloadTimeout = positive(downloadTimeout, "Download UI timeout");
    }

    private static Duration positive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive.");
        }

        return value;
    }
}
