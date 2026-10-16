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

    /** Clears an editable element. */
    default void clear() {
        fill("");
    }

    /** Focuses the element. */
    default void focus() {
        throw unsupported("focus");
    }

    /** Removes focus from the element. */
    default void blur() {
        throw unsupported("blur");
    }

    /** Hovers over the element. */
    default void hover() {
        throw unsupported("hover");
    }

    /** Checks a checkbox or radio control. */
    default void check() {
        throw unsupported("check");
    }

    /** Unchecks a checkbox control. */
    default void uncheck() {
        throw unsupported("uncheck");
    }

    /**
     * Presses a keyboard key.
     *
     * @param key engine-neutral key name.
     */
    default void press(String key) {
        throw unsupported("press");
    }

    /**
     * Drags this element to another element.
     *
     * @param target target adapter.
     */
    default void dragTo(UiElementAdapter target) {
        throw unsupported("dragTo");
    }

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
     * Returns an HTML attribute value.
     *
     * @param name attribute name.
     * @return attribute value, or {@code null} when absent.
     */
    default String attribute(String name) {
        throw unsupported("attribute");
    }

    /**
     * Returns a DOM property value converted to text.
     *
     * @param name property name.
     * @return property value, or {@code null} when absent.
     */
    default String property(String name) {
        throw unsupported("property");
    }

    /**
     * Returns the current value exposed by the underlying form control.
     *
     * @return current form-control value.
     */
    default String value() {
        throw unsupported("value");
    }

    /**
     * Returns the HTML tag name of the element in lower case.
     *
     * @return lower-case HTML tag name.
     */
    default String tagName() {
        throw unsupported("tagName");
    }

    /**
     * Checks whether the element is currently visible.
     *
     * @return whether element is currently visible.
     */
    boolean isVisible();

    /**
     * Checks whether the element is enabled for user interaction.
     *
     * @return whether element is enabled.
     */
    default boolean isEnabled() {
        throw unsupported("isEnabled");
    }

    /**
     * Checks whether the element currently accepts text editing.
     *
     * @return whether element is editable.
     */
    default boolean isEditable() {
        throw unsupported("isEditable");
    }

    /**
     * Checks the current checked state of a checkbox or radio control.
     *
     * @return whether checkbox or radio element is checked.
     */
    default boolean isChecked() {
        throw unsupported("isChecked");
    }

    /**
     * Checks whether an option-like element is currently selected.
     *
     * @return whether option-like element is selected.
     */
    default boolean isSelected() {
        throw unsupported("isSelected");
    }

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

    private static UnsupportedOperationException unsupported(String operation) {
        return new UnsupportedOperationException(
                "UI element adapter does not support operation: " + operation
        );
    }
}
