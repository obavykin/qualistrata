package com.bavykin.qualistrata.unix.session;

/**
 * Optional capability for Unix host sessions that can be explicitly invalidated.
 *
 * <p>Invalidation is different from normal close. Normal close usually means
 * that the consumer has finished using the session. In a pooled implementation,
 * this normally releases the borrowed session back to the pool.</p>
 *
 * <p>Invalidation means that the session is broken, unsafe, or should not be
 * reused. In a pooled implementation, invalidation should remove the underlying
 * session from the reusable pool and close the physical connection.</p>
 */
public interface InvalidatableUnixHostSession extends UnixHostSession {

    /**
     * Invalidates this session.
     *
     * <p>After invalidation, the session must not be returned to the reusable
     * pool and must not be used for further command execution.</p>
     */
    void invalidate();
}