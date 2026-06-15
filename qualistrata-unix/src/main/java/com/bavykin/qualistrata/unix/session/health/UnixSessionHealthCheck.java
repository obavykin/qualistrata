package com.bavykin.qualistrata.unix.session.health;

import com.bavykin.qualistrata.unix.session.UnixHostSession;

/**
 * Checks whether a Unix host session is healthy and safe to reuse.
 *
 * <p>Health checks are especially important for pooled sessions. A session may
 * still exist as an object but no longer be usable because the remote host
 * closed the connection, authentication state expired, or the underlying
 * transport became broken.</p>
 *
 * <p>The health check contract is intentionally generic. SSH/JSch-specific
 * implementations may perform protocol-level checks later.</p>
 */
public interface UnixSessionHealthCheck {

    /**
     * Checks whether the provided session is healthy and safe to reuse.
     *
     * @param session session to check.
     * @return {@code true} when the session can be reused.
     */
    boolean isHealthy(UnixHostSession session);
}