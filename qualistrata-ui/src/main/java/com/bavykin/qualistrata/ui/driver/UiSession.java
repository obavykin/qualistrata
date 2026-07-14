package com.bavykin.qualistrata.ui.driver;

import com.bavykin.qualistrata.ui.element.UiElement;
import com.bavykin.qualistrata.ui.element.UiElementCollection;
import com.bavykin.qualistrata.ui.locator.UiLocator;

/**
 * Driver-independent page/window session.
 *
 * <p>A session represents one active page, tab, or window. Product page objects
 * should depend on this interface rather than on Playwright {@code Page} or
 * Selenium {@code WebDriver} directly.</p>
 */
public interface UiSession extends AutoCloseable {

    /**
     * Opens an absolute or base-URL-relative URL.
     *
     * @param urlOrPath absolute URL or application-relative path to open.
     */
    void open(String urlOrPath);

    /**
     * Returns the current page URL.
     *
     * @return current page URL.
     */
    String url();

    /**
     * Returns the current page title.
     *
     * @return current page title.
     */
    String title();

    /**
     * Resolves locator to a single element facade.
     *
     * @param locator driver-independent element locator.
     * @return the resulting value.
     */
    UiElement element(UiLocator locator);

    /**
     * Resolves locator to collection facade.
     *
     * @param locator driver-independent element locator.
     * @return the resulting value.
     */
    UiElementCollection elements(UiLocator locator);

    /**
     * Returns the window and tab helper.
     *
     * @return window/new-tab helper.
     */
    WindowSupport windows();

    /**
     * Returns the download helper.
     *
     * @return download helper.
     */
    DownloadSupport downloads();

    /**
     * Returns active session capabilities.
     *
     * @return active session capabilities.
     */
    UiCapabilities capabilities();

    /** Closes this page/window session. */
    @Override
    void close();
}
