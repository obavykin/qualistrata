package com.bavykin.qualistrata.unix.command;

import java.time.Duration;

/**
 * Request for executing a Unix command.
 *
 * <p>The request describes only generic command execution parameters. It does
 * not contain SSH-specific details such as hostname, username, password, or
 * private key path. Connection resolution belongs to higher-level Unix session
 * or client implementations.</p>
 *
 * @param command command line to execute on the target Unix host.
 * @param timeout maximum time allowed for command execution.
 */
public record UnixCommandRequest(
        String command,
        Duration timeout
) {
}