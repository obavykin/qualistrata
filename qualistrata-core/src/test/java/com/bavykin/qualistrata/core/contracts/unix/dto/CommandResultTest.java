package com.bavykin.qualistrata.core.contracts.unix.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link CommandResult}.
 *
 * <p>The record is a transport-independent DTO used to pass Unix command
 * execution results from concrete Unix implementations to higher-level
 * framework code. The test verifies that exit code and captured streams are
 * preserved exactly.</p>
 */
class CommandResultTest {

    @Test
    void shouldExposeCommandExecutionResult() {
        CommandResult result = new CommandResult(
                0,
                "command output",
                ""
        );

        assertEquals(0, result.exitCode());
        assertEquals("command output", result.stdout());
        assertEquals("", result.stderr());
    }
}