package com.bavykin.qualistrata.ui.locator;

/**
 * Driver-independent locator strategy.
 */
public enum LocatorType {
    /** CSS selector locator. */
    CSS,
    /** XPath selector locator. */
    XPATH,
    /** Visible-text locator. */
    TEXT,
    /** Link locator. */
    LINK,
    /** Button locator. */
    BUTTON,
    /** Input locator resolved by its label. */
    INPUT_BY_LABEL,
    /** Label locator. */
    LABEL,
    /** Placeholder-text locator. */
    PLACEHOLDER,
    /** Test-id locator. */
    TEST_ID,
    /** Accessible-role locator. */
    ROLE
}
