package com.bavykin.qualistrata.unix.session;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;

/**
 * Consumer-facing wrapper for a borrowed pooled Unix session.
 *
 * <p>The wrapper delegates command execution to the borrowed physical session.
 * When the wrapper is closed, it releases the borrowed session back to the pool
 * instead of closing the physical connection directly.</p>
 *
 * <p>The wrapper is intentionally single-use from the lifecycle perspective:
 * after {@link #close()} is called, further command execution through this
 * wrapper is rejected.</p>
 */
class PooledUnixHostSession implements UnixHostSession {

    private final UnixHostSession delegate;
    private final UnixHostSessionPool pool;

    private boolean closed;

    /**
     * Creates a pooled session wrapper.
     *
     * @param delegate borrowed physical session.
     * @param pool pool that owns the borrowed session.
     */
    PooledUnixHostSession(
            UnixHostSession delegate,
            UnixHostSessionPool pool
    ) {
        if (delegate == null) {
            throw new UnixSessionException("Pooled Unix session delegate must not be null.");
        }

        if (pool == null) {
            throw new UnixSessionException("Pooled Unix session pool must not be null.");
        }

        this.delegate = delegate;
        this.pool = pool;
    }

    /**
     * Executes a command through the borrowed session.
     *
     * @param request command execution request.
     * @return command result.
     * @throws UnixSessionException if this wrapper has already been closed.
     */
    @Override
    public CommandResult execute(UnixCommandRequest request) {
        ensureOpen();

        return delegate.execute(request);
    }

    /**
     * Checks whether this wrapper is still usable and the delegate is open.
     *
     * @return {@code true} when wrapper and delegate are open.
     */
    @Override
    public boolean isOpen() {
        return !closed && delegate.isOpen();
    }

    /**
     * Releases the borrowed session back to the pool.
     *
     * <p>The operation is idempotent from the consumer perspective. Calling
     * close more than once releases the delegate only once.</p>
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        pool.release(delegate);
    }

    /**
     * Ensures this wrapper has not been closed.
     */
    private void ensureOpen() {
        if (closed) {
            throw new UnixSessionException("Pooled Unix session is already closed.");
        }
    }
}