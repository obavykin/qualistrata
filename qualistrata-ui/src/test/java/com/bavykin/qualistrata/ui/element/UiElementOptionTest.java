package com.bavykin.qualistrata.ui.element;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class UiElementOptionTest {

    @Test
    public void shouldExposeImmutableOptionValuesAndLabels() {
        StubElementAdapter adapter = new StubElementAdapter(
                new ArrayList<>(List.of("", "CA", "NY")),
                new ArrayList<>(List.of("All", "California", "New York"))
        );
        UiElement element = new UiElement(adapter);

        List<String> values = element.optionValues();
        List<String> labels = element.optionLabels();

        assertEquals(values, List.of("", "CA", "NY"));
        assertEquals(labels, List.of("All", "California", "New York"));
        expectThrows(UnsupportedOperationException.class, () -> values.add("TX"));
        expectThrows(UnsupportedOperationException.class, () -> labels.add("Texas"));
    }

    private static final class StubElementAdapter implements UiElementAdapter {
        private final List<String> values;
        private final List<String> labels;

        private StubElementAdapter(List<String> values, List<String> labels) {
            this.values = values;
            this.labels = labels;
        }

        @Override
        public void click() {
        }

        @Override
        public void fill(String value) {
        }

        @Override
        public void selectByValue(String value) {
        }

        @Override
        public void selectByLabel(String label) {
        }

        @Override
        public void selectByIndex(int index) {
        }

        @Override
        public List<String> optionValues() {
            return values;
        }

        @Override
        public List<String> optionLabels() {
            return labels;
        }

        @Override
        public String text() {
            return "";
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public void waitUntilVisible() {
        }

        @Override
        public int count() {
            return 1;
        }

        @Override
        public UiElementAdapter nth(int index) {
            return this;
        }
    }
}
