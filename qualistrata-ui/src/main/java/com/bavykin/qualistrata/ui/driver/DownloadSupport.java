package com.bavykin.qualistrata.ui.driver;

import java.time.Duration;

/**
 * Generic download-event helper owned by a UI session.
 */
public interface DownloadSupport {

    /**
     * Waits for a download triggered by action.
     *
     * @param action action that starts download.
     * @return download descriptor.
     */
    UiDownload waitForDownload(Runnable action);

    /**
     * Waits for a download triggered by action.
     *
     * @param action action that starts download.
     * @param timeout timeout for the download event.
     * @return download descriptor.
     */
    UiDownload waitForDownload(Runnable action, Duration timeout);
}
