package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.UiAutomationException;
import com.bavykin.qualistrata.ui.locator.LocatorResolver;
import com.bavykin.qualistrata.ui.locator.UiLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Resolves generic {@link UiLocator} values to Playwright locators.
 */
public final class PlaywrightLocatorResolver implements LocatorResolver<Locator> {

    private final Page page;

    /**
     * Creates resolver bound to a Playwright page.
     *
     * @param page Playwright page.
     */
    public PlaywrightLocatorResolver(Page page) {
        if (page == null) {
            throw new UiAutomationException("Playwright page must not be null.");
        }

        this.page = page;
    }

    @Override
    public Locator resolve(UiLocator locator) {
        if (locator == null) {
            throw new UiAutomationException("UI locator must not be null.");
        }

        return switch (locator.type()) {
            case CSS -> page.locator(locator.value());
            case XPATH -> page.locator("xpath=" + locator.value());
            case TEXT -> page.getByText(locator.value());
            case LINK -> page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(locator.value()));
            case BUTTON -> page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(locator.value()));
            case INPUT_BY_LABEL, LABEL -> page.getByLabel(locator.value());
            case PLACEHOLDER -> page.getByPlaceholder(locator.value());
            case TEST_ID -> page.getByTestId(locator.value());
            case ROLE -> page.getByRole(role(locator.role()), new Page.GetByRoleOptions().setName(locator.value()));
        };
    }

    private AriaRole role(String value) {
        try {
            return AriaRole.valueOf(value.trim().replace('-', '_').toUpperCase());
        } catch (RuntimeException exception) {
            throw new UiAutomationException("Unsupported ARIA role for Playwright: " + value, exception);
        }
    }
}
