package com.bavykin.qualistrata.ui.driver;

import java.time.Duration;

/**
 * Generic new-window/new-tab helper owned by a UI session.
 */
public interface WindowSupport {

    /**
     * Waits for a new window created by action.
     *
     * @param action action that opens a new window.
     * @return child UI session bound to the new window.
     */
    UiSession waitForNewWindow(Runnable action);

    /**
     * Waits for a new window created by action.
     *
     * @param action action that opens a new window.
     * @param timeout timeout for popup event.
     * @return child UI session bound to the new window.
     */
    UiSession waitForNewWindow(Runnable action, Duration timeout);
}
