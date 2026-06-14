package com.bavykin.qualistrata.core.config.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link UserConfig}.
 *
 * <p>The test verifies that the record exposes all configured user fields
 * without transformation. This is important because secret resolution and
 * authentication decisions must be handled by higher-level components, not by
 * the raw configuration model.</p>
 */
class UserConfigTest {

    @Test
    void shouldExposeConfiguredUserFields() {
        UserConfig config = new UserConfig(
                "oracle",
                "secret-placeholder",
                "/home/oracle/.ssh/id_rsa"
        );

        assertEquals("oracle", config.username());
        assertEquals("secret-placeholder", config.password());
        assertEquals("/home/oracle/.ssh/id_rsa", config.keyPath());
    }
}