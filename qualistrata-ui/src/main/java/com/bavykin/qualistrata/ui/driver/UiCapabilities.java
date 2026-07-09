package com.bavykin.qualistrata.ui.driver;

/**
 * Feature matrix of the selected browser engine implementation.
 *
 * <p>Playwright and Selenium do not expose identical capabilities. This model
 * lets framework services collect optional diagnostics only when the active
 * adapter can provide them reliably.</p>
 *
 * @param tracing whether engine supports execution tracing.
 * @param video whether engine supports video recording.
 * @param networkCapture whether engine supports structured network capture.
 * @param browserContexts whether engine supports isolated browser contexts.
 * @param multipleWindows whether engine supports waiting for popup/new-window events.
 * @param downloads whether engine supports download event handling.
 */
public record UiCapabilities(
        boolean tracing,
        boolean video,
        boolean networkCapture,
        boolean browserContexts,
        boolean multipleWindows,
        boolean downloads
) {
    /** Playwright capability set. */
    public static final UiCapabilities PLAYWRIGHT = new UiCapabilities(
            true, true, true, true, true, true
    );

    /** Conservative Selenium capability set used until a real adapter is provided. */
    public static final UiCapabilities SELENIUM_BASIC = new UiCapabilities(
            false, false, false, false, true, false
    );
}
