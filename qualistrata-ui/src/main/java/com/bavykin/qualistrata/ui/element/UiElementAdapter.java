package com.bavykin.qualistrata.ui.element;

import java.util.List;

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

    /**
     * Fills editable element with text.
     *
     * @param value value to enter or select.
     */
    void fill(String value);

    /**
     * Selects option by value in a native select-like control.
     *
     * @param value value to enter or select.
     */
    void selectByValue(String value);

    /**
     * Selects option by visible label in a native select-like control.
     *
     * @param label visible option label to select.
     */
    void selectByLabel(String label);

    /**
     * Selects option by zero-based index in a native select-like control.
     *
     * @param index zero-based option index.
     */
    void selectByIndex(int index);

    /**
     * Returns option value attributes in DOM order.
     *
     * @return option value attributes in DOM order.
     */
    List<String> optionValues();

    /**
     * Returns visible option labels in DOM order.
     *
     * @return visible option labels in DOM order.
     */
    List<String> optionLabels();

    /**
     * Returns visible element text.
     *
     * @return visible text.
     */
    String text();

    /**
     * Checks whether the element is currently visible.
     *
     * @return whether element is currently visible.
     */
    boolean isVisible();

    /** Waits until element is visible. */
    void waitUntilVisible();

    /**
     * Returns the represented element count.
     *
     * @return element count represented by this adapter.
     */
    int count();

    /**
     * Returns an adapter for item at index.
     *
     * @param index zero-based item index.
     * @return item adapter.
     */
    UiElementAdapter nth(int index);
}
