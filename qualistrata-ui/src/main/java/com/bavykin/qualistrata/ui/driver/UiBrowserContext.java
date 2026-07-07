package com.bavykin.qualistrata.ui.driver;

/**
 * Isolated browser context owned by one test or logical user session.
 */
public interface UiBrowserContext extends AutoCloseable {

    /**
     * Opens a new page/session inside this context.
     *
     * @return UI session.
     */
    UiSession newSession();

    /** Closes all pages and context resources. */
    @Override
    void close();
}
