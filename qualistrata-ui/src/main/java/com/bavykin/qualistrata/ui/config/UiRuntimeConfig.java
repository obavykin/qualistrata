package com.bavykin.qualistrata.ui.config;

/**
 * Runtime UI automation configuration resolved for one execution.
 *
 * <p>The record keeps only generic browser/runtime settings. Product login,
 * roles, navigation, and page-object decisions belong to consumer projects.</p>
 *
 * @param baseUrl base URL of the application under test.
 * @param engine selected automation engine.
 * @param browser selected browser family.
 * @param executionMode local or remote execution mode.
 * @param headless whether browser UI should be hidden.
 * @param timeouts timeout bundle.
 * @param screenshotOnFailure whether screenshots are collected for failed tests.
 * @param traceMode trace collection policy.
 * @param videoOnFailure whether video should be collected for failed tests.
 * @param maxParallelSessions maximum logical UI sessions expected by the run.
 */
public record UiRuntimeConfig(
        String baseUrl,
        BrowserEngine engine,
        UiBrowserType browser,
        UiExecutionMode executionMode,
        boolean headless,
        UiTimeouts timeouts,
        boolean screenshotOnFailure,
        UiTraceMode traceMode,
        boolean videoOnFailure,
        int maxParallelSessions
) {
    /** Creates validated runtime configuration. */
    public UiRuntimeConfig {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("UI base URL must not be blank.");
        }

        if (engine == null) {
            throw new IllegalArgumentException("Browser engine must not be null.");
        }

        if (browser == null) {
            throw new IllegalArgumentException("Browser type must not be null.");
        }

        if (executionMode == null) {
            throw new IllegalArgumentException("UI execution mode must not be null.");
        }

        if (timeouts == null) {
            throw new IllegalArgumentException("UI timeouts must not be null.");
        }

        if (traceMode == null) {
            throw new IllegalArgumentException("UI trace mode must not be null.");
        }

        if (maxParallelSessions < 1) {
            throw new IllegalArgumentException("Maximum parallel UI sessions must be at least 1.");
        }
    }

    /**
     * Creates a minimal Playwright/Chromium/headless configuration.
     *
     * @param baseUrl application base URL.
     * @return default runtime configuration.
     */
    public static UiRuntimeConfig defaults(String baseUrl) {
        return new UiRuntimeConfig(
                baseUrl,
                BrowserEngine.PLAYWRIGHT,
                UiBrowserType.CHROMIUM,
                UiExecutionMode.LOCAL,
                true,
                UiTimeouts.DEFAULT,
                true,
                UiTraceMode.ON_FAILURE,
                false,
                1
        );
    }
}
