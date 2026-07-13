package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.element.UiElementAdapter;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.SelectOption;

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
    public String text() {
        return locator.textContent();
    }

    @Override
    public boolean isVisible() {
        return locator.isVisible();
    }

    @Override
    public void waitUntilVisible() {
        locator.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
    }

    @Override
    public int count() {
        return locator.count();
    }

    @Override
    public UiElementAdapter nth(int index) {
        return new PlaywrightElementAdapter(locator.nth(index));
    }
}
