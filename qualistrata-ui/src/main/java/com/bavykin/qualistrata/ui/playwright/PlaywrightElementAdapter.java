package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.element.UiElementAdapter;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.List;

/**
 * Playwright implementation of the generic element adapter.
 */
public final class PlaywrightElementAdapter implements UiElementAdapter {

    private final Locator locator;

    /**
     * Creates adapter around Playwright locator.
     *
     * @param locator native Playwright locator.
     */
    public PlaywrightElementAdapter(Locator locator) {
        if (locator == null) {
            throw new IllegalArgumentException("Playwright locator must not be null.");
        }

        this.locator = locator;
    }

    @Override
    public void click() {
        locator.click();
    }

    @Override
    public void fill(String value) {
        locator.fill(value == null ? "" : value);
    }

    @Override
    public void clear() {
        locator.clear();
    }

    @Override
    public void focus() {
        locator.focus();
    }

    @Override
    public void blur() {
        locator.blur();
    }

    @Override
    public void hover() {
        locator.hover();
    }

    @Override
    public void check() {
        locator.check();
    }

    @Override
    public void uncheck() {
        locator.uncheck();
    }

    @Override
    public void press(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Keyboard key must not be blank.");
        }
        locator.press(key);
    }

    @Override
    public void dragTo(UiElementAdapter target) {
        if (!(target instanceof PlaywrightElementAdapter playwrightTarget)) {
            throw new IllegalArgumentException(
                    "Playwright drag target must be backed by PlaywrightElementAdapter."
            );
        }
        locator.dragTo(playwrightTarget.locator);
    }

    @Override
    public void selectByValue(String value) {
        locator.selectOption(value == null ? "" : value);
    }

    @Override
    public void selectByLabel(String label) {
        locator.selectOption(new SelectOption().setLabel(label == null ? "" : label));
    }

    @Override
    public void selectByIndex(int index) {
        locator.selectOption(new SelectOption().setIndex(index));
    }

    @Override
    public List<String> optionValues() {
        return locator.locator("option").all().stream()
                .map(option -> normalize(option.getAttribute("value")))
                .toList();
    }

    @Override
    public List<String> optionLabels() {
        return locator.locator("option").all().stream()
                .map(option -> normalize(option.textContent()))
                .toList();
    }

    @Override
    public String text() {
        return locator.textContent();
    }

    @Override
    public String attribute(String name) {
        return locator.getAttribute(name);
    }

    @Override
    public String property(String name) {
        Object result = locator.evaluate(
                "(element, propertyName) => element[propertyName]",
                name
        );
        return result == null ? null : String.valueOf(result);
    }

    @Override
    public String value() {
        Object result = locator.evaluate("""
                element => {
                    if ('value' in element) {
                        return element.value;
                    }
                    return element.textContent ?? '';
                }
                """);
        return result == null ? "" : String.valueOf(result);
    }

    @Override
    public String tagName() {
        Object result = locator.evaluate("element => element.tagName.toLowerCase()");
        return result == null ? "" : String.valueOf(result);
    }

    @Override
    public boolean isVisible() {
        return locator.isVisible();
    }

    @Override
    public boolean isEnabled() {
        return locator.isEnabled();
    }

    @Override
    public boolean isEditable() {
        return locator.isEditable();
    }

    @Override
    public boolean isChecked() {
        return locator.isChecked();
    }

    @Override
    public boolean isSelected() {
        Object result = locator.evaluate("""
                element => {
                    if ('selected' in element) {
                        return Boolean(element.selected);
                    }
                    if ('checked' in element) {
                        return Boolean(element.checked);
                    }
                    return false;
                }
                """);
        return Boolean.parseBoolean(String.valueOf(result));
    }

    @Override
    public void waitUntilVisible() {
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    @Override
    public int count() {
        return locator.count();
    }

    @Override
    public UiElementAdapter nth(int index) {
        return new PlaywrightElementAdapter(locator.nth(index));
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
