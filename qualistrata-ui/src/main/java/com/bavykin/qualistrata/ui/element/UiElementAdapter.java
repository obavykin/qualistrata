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

    /** Selects option by value in a native select-like control. */
    void selectByValue(String value);

    /** Selects option by visible label in a native select-like control. */
    void selectByLabel(String label);

    /** Selects option by zero-based index in a native select-like control. */
    void selectByIndex(int index);

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
