package com.bavykin.qualistrata.unix.environment.os;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link UnixOperatingSystemProfile}.
 */
class UnixOperatingSystemProfileTest {

    @Test
    void shouldNormalizeTextFields() {
        UnixOperatingSystemProfile profile = new UnixOperatingSystemProfile(
                " rhel ",
                9,
                " 9.5 "
        );

        assertEquals("rhel", profile.id());
        assertEquals(9, profile.major());
        assertEquals("9.5", profile.versionId());
    }

    @Test
    void shouldConvertBlankTextFieldsToNull() {
        UnixOperatingSystemProfile profile = new UnixOperatingSystemProfile(
                " ",
                null,
                " "
        );

        assertNull(profile.id());
        assertNull(profile.major());
        assertNull(profile.versionId());
    }

    @Test
    void shouldFailWhenMajorVersionIsNotPositive() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new UnixOperatingSystemProfile("rhel", 0, "9.5")
        );

        assertEquals("Unix operating system major version must be positive.", exception.getMessage());
    }
}