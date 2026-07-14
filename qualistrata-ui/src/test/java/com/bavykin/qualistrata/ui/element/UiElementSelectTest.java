package com.bavykin.qualistrata.ui.element;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Unit coverage for driver-independent select-control operations exposed by UiElement.
 */
public class UiElementSelectTest {

    @Test
    public void delegatesSelectByValueToAdapter() {
        RecordingAdapter adapter = new RecordingAdapter();
        UiElement element = new UiElement(adapter);

        element.selectByValue("200102");

        assertEquals(adapter.selectedByValue, "200102");
    }

    @Test
    public void delegatesSelectByLabelToAdapter() {
        RecordingAdapter adapter = new RecordingAdapter();
        UiElement element = new UiElement(adapter);

        element.selectByLabel("02/2001");

        assertEquals(adapter.selectedByLabel, "02/2001");
    }

    @Test
    public void delegatesSelectByIndexToAdapter() {
        RecordingAdapter adapter = new RecordingAdapter();
        UiElement element = new UiElement(adapter);

        element.selectByIndex(3);

        assertEquals(adapter.selectedByIndex, Integer.valueOf(3));
    }

    private static final class RecordingAdapter implements UiElementAdapter {

        private String selectedByValue;
        private String selectedByLabel;
        private Integer selectedByIndex;

        @Override
        public void click() {
            // Not used by select-operation tests.
        }

        @Override
        public void fill(String value) {
            // Not used by select-operation tests.
        }

        @Override
        public void selectByValue(String value) {
            selectedByValue = value;
        }

        @Override
        public void selectByLabel(String label) {
            selectedByLabel = label;
        }

        @Override
        public void selectByIndex(int index) {
            selectedByIndex = index;
        }

        @Override
        public List<String> optionValues() {
            return List.of();
        }

        @Override
        public List<String> optionLabels() {
            return List.of();
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
            // Not used by select-operation tests.
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
