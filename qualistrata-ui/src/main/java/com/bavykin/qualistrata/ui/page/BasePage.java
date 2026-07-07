package com.bavykin.qualistrata.ui.page;

import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.element.UiElement;
import com.bavykin.qualistrata.ui.element.UiElementCollection;
import com.bavykin.qualistrata.ui.locator.UiLocator;

/**
 * Generic base class for consumer-project page objects.
 *
 * <p>The class intentionally contains only browser/page primitives. It does not
 * know about reports, product navigation, authentication, roles, or business
 * workflows. Consumer modules may extend it with product-specific base pages.</p>
 */
public abstract class BasePage {

    protected final UiSession session;

    /**
     * Creates page object bound to a UI session.
     *
     * @param session active UI session.
     */
    protected BasePage(UiSession session) {
        if (session == null) {
            throw new IllegalArgumentException("UI session must not be null.");
        }

        this.session = session;
    }

    /** @return current page title. */
    public String title() {
        return session.title();
    }

    /** @return current page URL. */
    public String url() {
        return session.url();
    }

    /** Resolves single element. */
    protected UiElement element(UiLocator locator) {
        return session.element(locator);
    }

    /** Resolves element collection. */
    protected UiElementCollection elements(UiLocator locator) {
        return session.elements(locator);
    }

    /** Fails when current title does not contain expected text. */
    protected void titleShouldContain(String expectedText) {
        if (expectedText == null || expectedText.isBlank()) {
            throw new IllegalArgumentException("Expected title text must not be blank.");
        }

        String actualTitle = title();
        if (actualTitle == null || !actualTitle.contains(expectedText)) {
            throw new AssertionError("Expected page title to contain '" + expectedText + "' but was '" + actualTitle + "'.");
        }
    }
}
