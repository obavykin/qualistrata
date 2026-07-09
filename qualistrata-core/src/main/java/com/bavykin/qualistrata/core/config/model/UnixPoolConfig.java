package com.bavykin.qualistrata.core.config.model;

/**
 * Unix connection pool configuration.
 *
 * <p>This record defines generic connection reuse settings for Unix/SSH-based
 * framework operations. It belongs to the core configuration model because
 * multiple framework modules may need to understand how Unix connections should
 * be managed without depending on a concrete SSH implementation.</p>
 *
 * <p>The values are nullable by design because target configuration files may
 * omit pool settings and allow the concrete Unix module implementation to apply
 * sensible defaults.</p>
 *
 * @param size maximum number of reusable Unix connections managed by the pool.
 * @param keepAliveSeconds number of seconds an idle connection may remain alive
 *                         before it is eligible for validation, refresh, or
 *                         closure by the implementation.
 * @param borrowTimeoutSeconds maximum number of seconds to wait when borrowing
 *                             a connection from the pool before failing fast.
 */
public record UnixPoolConfig(
        Integer size,
        Integer keepAliveSeconds,
        Integer borrowTimeoutSeconds
) {
}