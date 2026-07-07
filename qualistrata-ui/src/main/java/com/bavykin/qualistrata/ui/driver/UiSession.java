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

    /** Opens an absolute or base-URL-relative URL. */
    void open(String urlOrPath);

    /** @return current page URL. */
    String url();

    /** @return current page title. */
    String title();

    /** Resolves locator to a single element facade. */
    UiElement element(UiLocator locator);

    /** Resolves locator to collection facade. */
    UiElementCollection elements(UiLocator locator);

    /** @return window/new-tab helper. */
    WindowSupport windows();

    /** @return download helper. */
    DownloadSupport downloads();

    /** @return active session capabilities. */
    UiCapabilities capabilities();

    /** Closes this page/window session. */
    @Override
    void close();
}
