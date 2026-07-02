package com.bavykin.qualistrata.unix.session.pool;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.interactive.InteractiveCommandException;
import com.bavykin.qualistrata.unix.interactive.InteractiveCommandResult;
import com.bavykin.qualistrata.unix.interactive.InteractiveCommandScript;
import com.bavykin.qualistrata.unix.session.InteractiveUnixHostSession;
import com.bavykin.qualistrata.unix.session.UnixHostSession;

/**
 * Internal pool-owned session handle.
 *
 * <p>The handle associates a physical session with the pool key used for
 * capacity accounting and reuse. It delegates all session operations to the
 * underlying physical session.</p>
 *
 * @param key pool key.
 * @param session physical session owned by the pool.
 */
record PooledSessionHandle(
        UnixSessionPoolKey key,
        UnixHostSession session
) implements InteractiveUnixHostSession {

    /**
     * Executes a command through the underlying physical session.
     *
     * @param request command execution request.
     * @return command result.
     */
    @Override
    public CommandResult execute(UnixCommandRequest request) {
        return session.execute(request);
    }

    /**
     * Executes an interactive command through the underlying physical session.
     *
     * @param script interactive command script.
     * @return interactive command result.
     */
    @Override
    public InteractiveCommandResult executeInteractive(InteractiveCommandScript script) {
        if (!(session instanceof InteractiveUnixHostSession interactiveSession)) {
            throw new InteractiveCommandException("Pooled session handle delegate does not support interactive command execution.");
        }

        return interactiveSession.executeInteractive(script);
    }

    /**
     * Checks whether the underlying physical session is open.
     *
     * @return {@code true} when the physical session is open.
     */
    @Override
    public boolean isOpen() {
        return session.isOpen();
    }

    /**
     * Closes the underlying physical session.
     */
    @Override
    public void close() {
        session.close();
    }
}