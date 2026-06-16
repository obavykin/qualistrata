package com.bavykin.qualistrata.unix.environment.os;

import com.bavykin.qualistrata.core.contracts.unix.dto.OsRelease;
import com.bavykin.qualistrata.unix.session.UnixSessionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link OsReleaseParser}.
 */
class OsReleaseParserTest {

    @Test
    void shouldParseOsReleaseContent() {
        String content = """
                NAME="Red Hat Enterprise Linux"
                VERSION="9.5 (Plow)"
                ID="rhel"
                VERSION_ID="9.5"
                PRETTY_NAME="Red Hat Enterprise Linux 9.5 (Plow)"
                """;

        OsRelease actual = new OsReleaseParser().parse(content);

        assertEquals("rhel", actual.id());
        assertEquals("Red Hat Enterprise Linux", actual.name());
        assertEquals("Red Hat Enterprise Linux 9.5 (Plow)", actual.prettyName());
        assertEquals("9.5 (Plow)", actual.version());
        assertEquals("9.5", actual.versionId());
        assertEquals("rhel", actual.raw().get("ID"));
    }

    @Test
    void shouldParseUnquotedValues() {
        String content = """
                ID=ubuntu
                VERSION_ID=24.04
                PRETTY_NAME=Ubuntu 24.04 LTS
                """;

        OsRelease actual = new OsReleaseParser().parse(content);

        assertEquals("ubuntu", actual.id());
        assertEquals("24.04", actual.versionId());
        assertEquals("Ubuntu 24.04 LTS", actual.prettyName());
    }

    @Test
    void shouldIgnoreBlankCommentAndInvalidLines() {
        String content = """
                
                # comment
                INVALID_LINE
                ID="rhel"
                VERSION_ID="9.5"
                """;

        OsRelease actual = new OsReleaseParser().parse(content);

        assertEquals("rhel", actual.id());
        assertEquals("9.5", actual.versionId());
    }

    @Test
    void shouldFailWhenContentIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new OsReleaseParser().parse(" ")
        );

        assertEquals("OS release content must not be blank.", exception.getMessage());
    }
}