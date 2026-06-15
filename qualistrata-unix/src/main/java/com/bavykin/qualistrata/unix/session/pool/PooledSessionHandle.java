package com.bavykin.qualistrata.unix.session.pool;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
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
) implements UnixHostSession {

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