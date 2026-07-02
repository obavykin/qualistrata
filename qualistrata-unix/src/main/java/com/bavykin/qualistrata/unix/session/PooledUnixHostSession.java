package com.bavykin.qualistrata.unix.session;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.interactive.InteractiveCommandException;
import com.bavykin.qualistrata.unix.interactive.InteractiveCommandResult;
import com.bavykin.qualistrata.unix.interactive.InteractiveCommandScript;

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
class PooledUnixHostSession implements InvalidatableUnixHostSession, InteractiveUnixHostSession  {

    private final UnixHostSession delegate;
    private final UnixHostSessionPool pool;

    private boolean invalidated;
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
     * Executes an interactive command through the borrowed session.
     *
     * @param script interactive command script.
     * @return interactive command result.
     */
    @Override
    public InteractiveCommandResult executeInteractive(InteractiveCommandScript script) {
        ensureOpen();

        if (!(delegate instanceof InteractiveUnixHostSession interactiveSession)) {
            throw new InteractiveCommandException("Pooled Unix session delegate does not support interactive command execution.");
        }

        return interactiveSession.executeInteractive(script);
    }

    /**
     * Checks whether this wrapper is still usable and the delegate is open.
     *
     * @return {@code true} when wrapper and delegate are open.
     */
    @Override
    public boolean isOpen() {
        return !closed && !invalidated && delegate.isOpen();
    }

    /**
     * Invalidates the borrowed session.
     *
     * <p>Invalidation is used when the borrowed session is broken or unsafe for
     * reuse. The delegate is passed to the pool invalidation path and must not be
     * returned to the idle session queue.</p>
     *
     * <p>The operation is idempotent from the consumer perspective. Calling
     * invalidate more than once invalidates the delegate only once.</p>
     */
    @Override
    public void invalidate() {
        if (closed || invalidated) {
            return;
        }

        invalidated = true;

        pool.invalidate(delegate);
    }

    /**
     * Releases the borrowed session back to the pool.
     *
     * <p>The operation is idempotent from the consumer perspective. Calling close
     * more than once releases the delegate only once.</p>
     *
     * <p>If the session was already invalidated, close does nothing because the
     * delegate has already been removed from the reusable pool.</p>
     */
    @Override
    public void close() {
        if (closed || invalidated) {
            return;
        }

        closed = true;

        pool.release(delegate);
    }

    /**
     * Ensures this wrapper has not been closed or invalidated.
     */
    private void ensureOpen() {
        if (closed) {
            throw new UnixSessionException("Pooled Unix session is already closed.");
        }

        if (invalidated) {
            throw new UnixSessionException("Pooled Unix session is already invalidated.");
        }
    }
}