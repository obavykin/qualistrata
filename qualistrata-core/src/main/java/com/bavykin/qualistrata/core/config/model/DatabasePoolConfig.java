package com.bavykin.qualistrata.core.config.model;

/**
 * Optional database connection pool settings.
 *
 * <p>Pooling is deliberately modeled in core because it is a generic resource
 * access concern, but concrete pool implementation belongs to the database
 * module. Pooling should remain disabled by default for simple one-off checks.</p>
 *
 * @param enabled whether pooling is enabled for this database alias.
 * @param size maximum number of connections in the pool.
 * @param borrowTimeoutSeconds maximum time to wait when borrowing from the pool.
 * @param idleTimeoutSeconds maximum time an idle physical connection may stay
 *                           open before it can be closed by implementation.
 */
public record DatabasePoolConfig(
        Boolean enabled,
        Integer size,
        Integer borrowTimeoutSeconds,
        Integer idleTimeoutSeconds
) {
}
