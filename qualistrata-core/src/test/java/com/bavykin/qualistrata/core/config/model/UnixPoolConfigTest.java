package com.bavykin.qualistrata.core.config.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link UnixPoolConfig}.
 *
 * <p>The test keeps the configuration model intentionally simple: the record
 * stores pool settings exactly as they were loaded. Validation and defaulting
 * rules should be implemented by configuration services or Unix module
 * components, not by this DTO.</p>
 */
class UnixPoolConfigTest {

    @Test
    void shouldExposeConfiguredPoolSettings() {
        UnixPoolConfig config = new UnixPoolConfig(
                5,
                60,
                10
        );

        assertEquals(5, config.size());
        assertEquals(60, config.keepAliveSeconds());
        assertEquals(10, config.borrowTimeoutSeconds());
    }
}