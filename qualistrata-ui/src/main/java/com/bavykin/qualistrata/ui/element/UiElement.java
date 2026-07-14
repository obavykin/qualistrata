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
     * Checks whether the element is currently visible.
     *
     * @return whether element is currently visible.
     */
    public boolean isVisible() {
        return adapter.isVisible();
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
}
