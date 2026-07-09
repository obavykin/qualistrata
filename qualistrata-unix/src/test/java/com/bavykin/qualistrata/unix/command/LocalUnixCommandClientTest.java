package com.bavykin.qualistrata.unix.command;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link LocalUnixCommandClient}.
 *
 * <p>The tests verify the local process implementation of the generic Unix
 * command contract. Commands are intentionally simple and cross-platform so
 * that they can run on developer machines and CI agents.</p>
 */
class LocalUnixCommandClientTest {

    private final LocalUnixCommandClient client = new LocalUnixCommandClient();

    @Test
    void shouldExecuteSuccessfulCommand() {
        CommandResult result = client.execute(new UnixCommandRequest(
                echoCommand("qualistrata"),
                Duration.ofSeconds(5)
        ));

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("qualistrata"));
        assertEquals("", result.stderr());
    }

    @Test
    void shouldReturnNonZeroExitCodeAsCommandResult() {
        CommandResult result = client.execute(new UnixCommandRequest(
                exitCommand(7),
                Duration.ofSeconds(5)
        ));

        assertEquals(7, result.exitCode());
    }

    @Test
    void shouldCaptureStandardError() {
        CommandResult result = client.execute(new UnixCommandRequest(
                stderrCommand("qualistrata-error"),
                Duration.ofSeconds(5)
        ));

        assertEquals(0, result.exitCode());
        assertTrue(result.stderr().contains("qualistrata-error"));
    }

    @Test
    void shouldFailWhenCommandTimesOut() {
        UnixCommandException exception = assertThrows(
                UnixCommandException.class,
                () -> client.execute(new UnixCommandRequest(
                        sleepCommand(5),
                        Duration.ofMillis(100)
                ))
        );

        assertEquals("Local Unix command timed out after: PT0.1S", exception.getMessage());
    }

    @Test
    void shouldValidateRequestBeforeExecution() {
        UnixCommandException exception = assertThrows(
                UnixCommandException.class,
                () -> client.execute(new UnixCommandRequest(
                        " ",
                        Duration.ofSeconds(5)
                ))
        );

        assertEquals("Unix command must not be blank.", exception.getMessage());
    }

    /**
     * Builds a cross-platform command that writes text to stdout.
     *
     * @param text text to print.
     * @return command line.
     */
    private String echoCommand(String text) {
        return isWindows()
                ? "echo " + text
                : "printf '%s' '" + text + "'";
    }

    /**
     * Builds a cross-platform command that exits with the requested exit code.
     *
     * @param exitCode process exit code.
     * @return command line.
     */
    private String exitCommand(int exitCode) {
        return isWindows()
                ? "exit /b " + exitCode
                : "exit " + exitCode;
    }

    /**
     * Builds a cross-platform command that writes text to stderr.
     *
     * @param text text to print to stderr.
     * @return command line.
     */
    private String stderrCommand(String text) {
        return isWindows()
                ? "echo " + text + " 1>&2"
                : "printf '%s' '" + text + "' 1>&2";
    }

    /**
     * Builds a cross-platform command that sleeps for the requested number of
     * seconds.
     *
     * @param seconds sleep duration in seconds.
     * @return command line.
     */
    private String sleepCommand(int seconds) {
        return isWindows()
                ? "ping -n " + (seconds + 1) + " 127.0.0.1 > nul"
                : "sleep " + seconds;
    }

    /**
     * Detects whether the current JVM is running on Windows.
     *
     * @return {@code true} when current operating system looks like Windows.
     */
    private boolean isWindows() {
        String osName = System.getProperty("os.name");

        return osName != null && osName.toLowerCase().contains("win");
    }
}