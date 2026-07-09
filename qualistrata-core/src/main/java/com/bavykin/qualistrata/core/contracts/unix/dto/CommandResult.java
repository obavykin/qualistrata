package com.bavykin.qualistrata.core.contracts.unix.dto;

/**
 * Result of a Unix command execution.
 *
 * <p>This record is a generic data transfer object shared between the core
 * framework contracts and concrete Unix/SSH implementations. It intentionally
 * contains only process-level execution output and does not make assumptions
 * about the transport mechanism used to run the command.</p>
 *
 * @param exitCode process exit code returned by the executed command.
 * @param stdout standard output captured from the command.
 * @param stderr standard error output captured from the command.
 */
public record CommandResult(
        int exitCode,
        String stdout,
        String stderr
) {
}