package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.driver.DownloadSupport;
import com.bavykin.qualistrata.ui.driver.UiDownload;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;

import java.time.Duration;

/**
 * Playwright download helper.
 */
final class PlaywrightDownloadSupport implements DownloadSupport {

    private final Page page;
    private final Duration defaultTimeout;

    PlaywrightDownloadSupport(Page page, Duration defaultTimeout) {
        this.page = page;
        this.defaultTimeout = defaultTimeout;
    }

    @Override
    public UiDownload waitForDownload(Runnable action) {
        return waitForDownload(action, defaultTimeout);
    }

    @Override
    public UiDownload waitForDownload(Runnable action, Duration timeout) {
        Page.WaitForDownloadOptions options = new Page.WaitForDownloadOptions()
                .setTimeout(timeout.toMillis());
        Download download = page.waitForDownload(options, action);
        return new UiDownload(download.suggestedFilename(), download.path());
    }
}
