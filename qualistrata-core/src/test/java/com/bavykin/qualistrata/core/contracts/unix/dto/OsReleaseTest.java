package com.bavykin.qualistrata.core.contracts.unix.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link OsRelease}.
 *
 * <p>The record represents parsed operating system release information. The
 * test verifies both normalized fields and the raw map because product-specific
 * validators may need access to distribution-specific values that are not part
 * of the common model.</p>
 */
class OsReleaseTest {

    @Test
    void shouldExposeParsedOsReleaseFieldsAndRawValues() {
        Map<String, String> raw = Map.of(
                "ID", "rhel",
                "VERSION_ID", "8.10"
        );

        OsRelease release = new OsRelease(
                "rhel",
                "Red Hat Enterprise Linux",
                "Red Hat Enterprise Linux 8.10",
                "8.10",
                "8.10",
                raw
        );

        assertEquals("rhel", release.id());
        assertEquals("Red Hat Enterprise Linux", release.name());
        assertEquals("Red Hat Enterprise Linux 8.10", release.prettyName());
        assertEquals("8.10", release.version());
        assertEquals("8.10", release.versionId());
        assertEquals(raw, release.raw());
    }
}