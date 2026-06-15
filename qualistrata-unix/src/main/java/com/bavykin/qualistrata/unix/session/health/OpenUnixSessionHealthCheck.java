package com.bavykin.qualistrata.unix.session.health;

import com.bavykin.qualistrata.unix.session.UnixHostSession;

/**
 * Basic session health check that treats open sessions as healthy.
 *
 * <p>This implementation is intentionally simple and dependency-free. It is a
 * safe default before protocol-specific SSH health checks are introduced.</p>
 */
public class OpenUnixSessionHealthCheck implements UnixSessionHealthCheck {

    /**
     * Creates an open-session health check.
     */
    public OpenUnixSessionHealthCheck() {
    }

    /**
     * Checks whether the session is non-null and open.
     *
     * @param session session to check.
     * @return {@code true} when the session is non-null and open.
     */
    @Override
    public boolean isHealthy(UnixHostSession session) {
        return session != null && session.isOpen();
    }
}