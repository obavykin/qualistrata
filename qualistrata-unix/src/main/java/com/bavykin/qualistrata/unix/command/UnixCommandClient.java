package com.bavykin.qualistrata.unix.command;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;

/**
 * Contract for executing Unix commands.
 *
 * <p>The contract is intentionally small and implementation-neutral. It can be
 * implemented by SSH-based clients, local process clients, mock clients used by
 * tests, or future container-based execution adapters.</p>
 *
 * <p>The interface returns {@link CommandResult} instead of throwing on
 * non-zero exit code. A non-zero exit code is a valid command result and should
 * be interpreted by the caller. Implementations should throw
 * {@link UnixCommandException} only when the command could not be executed or
 * when execution infrastructure failed.</p>
 */
public interface UnixCommandClient {

    /**
     * Executes a Unix command.
     *
     * @param request command execution request.
     * @return command execution result containing exit code, stdout, and stderr.
     * @throws UnixCommandException if the command cannot be executed because of
     *                              infrastructure, connection, timeout, or
     *                              client-level failure.
     */
    CommandResult execute(UnixCommandRequest request);
}