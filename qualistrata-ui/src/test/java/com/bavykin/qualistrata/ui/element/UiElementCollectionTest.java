package com.bavykin.qualistrata.ui.element;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for element collection facade without launching a browser.
 */
class UiElementCollectionTest {

    @Test
    void shouldReadTextsFromAllElements() {
        UiElementCollection collection = new UiElementCollection(new FakeAdapter(List.of("A", "B")));

        assertEquals(List.of("A", "B"), collection.texts());
    }

    @Test
    void shouldRejectIndexOutsideCollection() {
        UiElementCollection collection = new UiElementCollection(new FakeAdapter(List.of("A")));

        assertThrows(IndexOutOfBoundsException.class, () -> collection.nth(1));
    }

    private static final class FakeAdapter implements UiElementAdapter {
        private final List<String> values;
        private final int index;

        private FakeAdapter(List<String> values) {
            this(values, -1);
        }

        private FakeAdapter(List<String> values, int index) {
            this.values = values;
            this.index = index;
        }

        @Override public void click() { }
        @Override public void fill(String value) { }
        @Override public String text() { return values.get(index); }
        @Override public boolean isVisible() { return true; }
        @Override public void waitUntilVisible() { }
        @Override public int count() { return values.size(); }
        @Override public UiElementAdapter nth(int index) { return new FakeAdapter(values, index); }
    }
}
