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

    /**
     * Downloads a resource through the active browser context.
     *
     * <p>This operation reuses the browser context request client and therefore
     * carries the authenticated browser-context cookies. It is intended for
     * application exports implemented as direct navigation rather than as a
     * browser download event.</p>
     *
     * @param url absolute or current-page-relative resource URL.
     * @return downloaded resource descriptor.
     */
    UiDownload downloadFromUrl(String url);
}
