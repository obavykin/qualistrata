package com.bavykin.qualistrata.ui.driver;

/**
 * Browser-engine runtime owner.
 *
 * <p>A driver usually owns heavy engine-level resources such as Playwright
 * runtime and browser instance. Tests should create lightweight contexts or
 * sessions from it instead of launching a fresh browser for every method.</p>
 */
public interface UiDriver extends AutoCloseable {

    /**
     * Creates an isolated browser context for a logical test/user session.
     *
     * @return browser context.
     */
    UiBrowserContext newContext();

    /**
     * Returns capabilities of this driver implementation.
     *
     * @return driver capabilities.
     */
    UiCapabilities capabilities();

    /** Closes engine resources. */
    @Override
    void close();
}
