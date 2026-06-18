package com.bavykin.qualistrata.unix.environment.detector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link UnixShellProfileCommandBuilder}.
 */
class UnixShellProfileCommandBuilderTest {

    private final UnixShellProfileCommandBuilder builder = new UnixShellProfileCommandBuilder();

    @Test
    void shouldReturnCommandWhenProfileIsMissing() {
        String command = builder.build(" python3 --version ", null);

        assertEquals("python3 --version", command);
    }

    @Test
    void shouldReturnCommandWhenProfileIsBlank() {
        String command = builder.build("python3 --version", "   ");

        assertEquals("python3 --version", command);
    }

    @Test
    void shouldSourceProfileBeforeCommand() {
        String command = builder.build(
                "python3 --version",
                "/opt/app/.profile"
        );

        assertEquals(". '/opt/app/.profile' && python3 --version", command);
    }

    @Test
    void shouldEscapeSingleQuotesInProfilePath() {
        String command = builder.build(
                "python3 --version",
                "/opt/app/profile's/.profile"
        );

        assertEquals(". '/opt/app/profile'\"'\"'s/.profile' && python3 --version", command);
    }

    @Test
    void shouldFailWhenCommandIsBlank() {
        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> builder.build(" ", "/opt/app/.profile")
        );

        assertEquals("Unix detector command must not be blank.", exception.getMessage());
    }
}