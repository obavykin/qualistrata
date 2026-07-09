package com.bavykin.qualistrata.ui.element;

import java.util.ArrayList;
import java.util.List;

/**
 * Driver-independent collection of matching UI elements.
 */
public final class UiElementCollection {

    private final UiElementAdapter adapter;

    /**
     * Creates collection facade.
     *
     * @param adapter native adapter that may represent multiple elements.
     */
    public UiElementCollection(UiElementAdapter adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("UI element collection adapter must not be null.");
        }

        this.adapter = adapter;
    }

    /** @return current element count. */
    public int count() {
        return adapter.count();
    }

    /**
     * Returns item at index.
     *
     * @param index zero-based item index.
     * @return UI element.
     */
    public UiElement nth(int index) {
        if (index < 0 || index >= count()) {
            throw new IndexOutOfBoundsException("UI element index out of range: " + index);
        }

        return new UiElement(adapter.nth(index));
    }

    /** @return visible text of all matched elements at the moment of call. */
    public List<String> texts() {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < count(); i++) {
            values.add(nth(i).text());
        }

        return values;
    }
}
