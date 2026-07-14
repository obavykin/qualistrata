package com.bavykin.qualistrata.ui.locator;

/**
 * Driver-independent locator used by page objects and components.
 *
 * <p>The locator describes the user's intent instead of exposing a concrete
 * Playwright or Selenium selector object. Adapter-specific resolvers translate
 * this value into the native engine representation.</p>
 *
 * @param type locator strategy.
 * @param value locator value.
 * @param role optional ARIA role name for {@link LocatorType#ROLE}.
 */
public record UiLocator(
        LocatorType type,
        String value,
        String role
) {
    /**
     * Creates a validated locator.
     */
    public UiLocator {
        if (type == null) {
            throw new IllegalArgumentException("Locator type must not be null.");
        }

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Locator value must not be blank.");
        }
    }

    /**
     * Creates a CSS locator.
     *
     * @param selector CSS selector.
     * @return CSS locator.
     */
    public static UiLocator css(String selector) {
        return new UiLocator(LocatorType.CSS, selector, null);
    }

    /**
     * Creates an XPath locator.
     *
     * @param expression XPath expression.
     * @return XPath locator.
     */
    public static UiLocator xpath(String expression) {
        return new UiLocator(LocatorType.XPATH, expression, null);
    }

    /**
     * Creates a visible-text locator.
     *
     * @param text visible text.
     * @return text locator.
     */
    public static UiLocator text(String text) {
        return new UiLocator(LocatorType.TEXT, text, null);
    }

    /**
     * Creates a link-text locator.
     *
     * @param text link text.
     * @return link locator.
     */
    public static UiLocator link(String text) {
        return new UiLocator(LocatorType.LINK, text, null);
    }

    /**
     * Creates a button-text locator.
     *
     * @param text button text.
     * @return button locator.
     */
    public static UiLocator button(String text) {
        return new UiLocator(LocatorType.BUTTON, text, null);
    }

    /**
     * Creates an input-by-label locator.
     *
     * @param label input label.
     * @return input-by-label locator.
     */
    public static UiLocator inputByLabel(String label) {
        return new UiLocator(LocatorType.INPUT_BY_LABEL, label, null);
    }

    /**
     * Creates a label-text locator.
     *
     * @param label label text.
     * @return label locator.
     */
    public static UiLocator label(String label) {
        return new UiLocator(LocatorType.LABEL, label, null);
    }

    /**
     * Creates a placeholder locator.
     *
     * @param placeholder input placeholder.
     * @return placeholder locator.
     */
    public static UiLocator placeholder(String placeholder) {
        return new UiLocator(LocatorType.PLACEHOLDER, placeholder, null);
    }

    /**
     * Creates a test-id locator.
     *
     * @param testId test id value.
     * @return test-id locator.
     */
    public static UiLocator testId(String testId) {
        return new UiLocator(LocatorType.TEST_ID, testId, null);
    }

    /**
     * Creates an ARIA-role locator.
     *
     * @param role ARIA role name supported by the selected adapter.
     * @param name accessible name.
     * @return role locator.
     */
    public static UiLocator role(String role, String name) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("ARIA role must not be blank.");
        }

        return new UiLocator(LocatorType.ROLE, name, role);
    }
}
