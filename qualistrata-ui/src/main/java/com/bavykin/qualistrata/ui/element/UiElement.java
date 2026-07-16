package com.bavykin.qualistrata.ui.element;

import com.bavykin.qualistrata.ui.UiAutomationException;

import java.util.List;

/**
 * Driver-independent UI element facade.
 */
public final class UiElement {

    private final UiElementAdapter adapter;

    /**
     * Creates UI element facade.
     *
     * @param adapter native element adapter.
     */
    public UiElement(UiElementAdapter adapter) {
        if (adapter == null) {
            throw new UiAutomationException("UI element adapter must not be null.");
        }

        this.adapter = adapter;
    }

    /** Clicks the element. */
    public void click() {
        adapter.click();
    }

    /**
     * Fills editable element.
     *
     * @param value text to enter.
     */
    public void fill(String value) {
        adapter.fill(value);
    }

    /** Clears an editable element. */
    public void clear() {
        adapter.clear();
    }

    /** Focuses the element. */
    public void focus() {
        adapter.focus();
    }

    /** Removes focus from the element. */
    public void blur() {
        adapter.blur();
    }

    /** Hovers over the element. */
    public void hover() {
        adapter.hover();
    }

    /** Checks a checkbox or radio control. */
    public void check() {
        adapter.check();
    }

    /** Unchecks a checkbox control. */
    public void uncheck() {
        adapter.uncheck();
    }

    /**
     * Presses a keyboard key while the element is focused.
     *
     * @param key engine-neutral key name such as Enter, Escape or Tab.
     */
    public void press(String key) {
        adapter.press(key);
    }

    /**
     * Drags this element to another element.
     *
     * @param target target element.
     */
    public void dragTo(UiElement target) {
        if (target == null) {
            throw new UiAutomationException("Drag target element must not be null.");
        }
        adapter.dragTo(target.adapter);
    }

    /**
     * Selects an option in a native select-like control by option value.
     *
     * @param value option value attribute to select.
     */
    public void selectByValue(String value) {
        adapter.selectByValue(value);
    }

    /**
     * Selects an option in a native select-like control by visible option label.
     *
     * @param label visible option label to select.
     */
    public void selectByLabel(String label) {
        adapter.selectByLabel(label);
    }

    /**
     * Selects an option in a native select-like control by zero-based option index.
     *
     * @param index zero-based option index to select.
     */
    public void selectByIndex(int index) {
        adapter.selectByIndex(index);
    }

    /**
     * Returns option value attributes from a native select-like control.
     *
     * @return immutable option values in DOM order.
     */
    public List<String> optionValues() {
        return List.copyOf(adapter.optionValues());
    }

    /**
     * Returns visible option labels from a native select-like control.
     *
     * @return immutable option labels in DOM order.
     */
    public List<String> optionLabels() {
        return List.copyOf(adapter.optionLabels());
    }

    /**
     * Returns visible element text.
     *
     * @return visible text.
     */
    public String text() {
        return adapter.text();
    }

    /**
     * Returns an HTML attribute value.
     *
     * @param name attribute name.
     * @return attribute value, or {@code null} when absent.
     */
    public String attribute(String name) {
        requireName(name, "Attribute");
        return adapter.attribute(name);
    }

    /**
     * Returns a DOM property value converted to text.
     *
     * @param name property name.
     * @return property value, or {@code null} when absent.
     */
    public String property(String name) {
        requireName(name, "Property");
        return adapter.property(name);
    }

    /**
     * Returns the current value exposed by the underlying form control.
     *
     * @return current form-control value.
     */
    public String value() {
        return adapter.value();
    }

    /**
     * Returns the HTML tag name of the element in lower case.
     *
     * @return lower-case HTML tag name.
     */
    public String tagName() {
        return adapter.tagName();
    }

    /**
     * Checks whether the element is currently visible.
     *
     * @return whether element is currently visible.
     */
    public boolean isVisible() {
        return adapter.isVisible();
    }

    /**
     * Checks whether the element is enabled for user interaction.
     *
     * @return whether element is enabled.
     */
    public boolean isEnabled() {
        return adapter.isEnabled();
    }

    /**
     * Checks whether the element currently accepts text editing.
     *
     * @return whether element is editable.
     */
    public boolean isEditable() {
        return adapter.isEditable();
    }

    /**
     * Checks the current checked state of a checkbox or radio control.
     *
     * @return whether checkbox or radio element is checked.
     */
    public boolean isChecked() {
        return adapter.isChecked();
    }

    /**
     * Checks whether an option-like element is currently selected.
     *
     * @return whether option-like element is selected.
     */
    public boolean isSelected() {
        return adapter.isSelected();
    }

    /** Waits until element is visible. */
    public void waitUntilVisible() {
        adapter.waitUntilVisible();
    }

    /**
     * Fails when element is not visible.
     *
     * @return this element for fluent page object code.
     */
    public UiElement shouldBeVisible() {
        waitUntilVisible();
        if (!isVisible()) {
            throw new UiAutomationException("Expected UI element to be visible.");
        }

        return this;
    }

    /**
     * Returns the underlying element adapter.
     *
     * @return underlying adapter for framework extensions.
     */
    public UiElementAdapter adapter() {
        return adapter;
    }

    private static void requireName(String name, String kind) {
        if (name == null || name.isBlank()) {
            throw new UiAutomationException(kind + " name must not be blank.");
        }
    }
}
