package com.bavykin.qualistrata.ui.locator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for driver-independent UI locators.
 */
class UiLocatorTest {

    @Test
    void shouldCreateBusinessReadableLocators() {
        assertEquals(LocatorType.BUTTON, UiLocator.button("Submit").type());
        assertEquals(LocatorType.LINK, UiLocator.link("Reports").type());
        assertEquals(LocatorType.INPUT_BY_LABEL, UiLocator.inputByLabel("Username").type());
    }

    @Test
    void shouldRejectBlankLocatorValue() {
        assertThrows(IllegalArgumentException.class, () -> UiLocator.text(" "));
    }

    @Test
    void shouldCreateRoleLocator() {
        UiLocator locator = UiLocator.role("heading", "Dashboard");

        assertEquals(LocatorType.ROLE, locator.type());
        assertEquals("heading", locator.role());
        assertEquals("Dashboard", locator.value());
    }
}
