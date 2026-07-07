package com.bavykin.qualistrata.ui.element;

/**
 * Native-engine adapter behind {@link UiElement}.
 *
 * <p>Page objects work with {@link UiElement}; Playwright and Selenium details
 * stay behind this interface. This keeps the public framework API stable when
 * an adapter implementation changes.</p>
 */
public interface UiElementAdapter {

    /** Clicks the element. */
    void click();

    /** Fills editable element with text. */
    void fill(String value);

    /** @return visible text. */
    String text();

    /** @return whether element is currently visible. */
    boolean isVisible();

    /** Waits until element is visible. */
    void waitUntilVisible();

    /** @return element count represented by this adapter. */
    int count();

    /**
     * Returns an adapter for item at index.
     *
     * @param index zero-based item index.
     * @return item adapter.
     */
    UiElementAdapter nth(int index);
}
