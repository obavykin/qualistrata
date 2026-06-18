package com.bavykin.qualistrata.unix.command;

import com.bavykin.qualistrata.unix.session.UnixSessionException;
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
    void shouldSourceProfileBeforeCommandAndRedirectProfileOutput() {
        String command = builder.build(
                "sqlplus -v",
                "/home/oracle/.profile"
        );

        assertEquals(". '/home/oracle/.profile' >/dev/null 2>&1 && sqlplus -v", command);
    }

    @Test
    void shouldTrimProfilePath() {
        String command = builder.build(
                "cat /etc/os-release",
                "  /opt/app/.profile  "
        );

        assertEquals(". '/opt/app/.profile' >/dev/null 2>&1 && cat /etc/os-release", command);
    }

    @Test
    void shouldUsePosixDotSyntaxInsteadOfSource() {
        String command = builder.build(
                "python3 --version",
                "/opt/app/.profile"
        );

        assertEquals(true, command.startsWith(". "));
        assertEquals(false, command.startsWith("source "));
    }

    @Test
    void shouldEscapeSingleQuotesInProfilePath() {
        String command = builder.build(
                "python3 --version",
                "/opt/app/profile's/.profile"
        );

        assertEquals(
                ". '/opt/app/profile'\"'\"'s/.profile' >/dev/null 2>&1 && python3 --version",
                command
        );
    }

    @Test
    void shouldFailWhenCommandIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> builder.build(" ", "/opt/app/.profile")
        );

        assertEquals("Unix command must not be blank.", exception.getMessage());
    }
}