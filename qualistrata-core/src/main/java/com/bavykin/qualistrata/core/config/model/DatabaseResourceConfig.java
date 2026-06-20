package com.bavykin.qualistrata.core.config.model;

/**
 * Generic database resource configuration.
 *
 * <p>The resource describes where the database is and how the database module
 * should interpret the endpoint. Credentials are resolved from
 * {@code secrets.yml} through an access profile.</p>
 *
 * @param type database engine type such as {@code oracle} or {@code postgresql}.
 * @param host database host.
 * @param port database listener port.
 * @param databaseName database name, Oracle SID, Oracle service name, or other
 *                     provider-specific database identifier.
 * @param schema optional default schema.
 * @param jdbcUrl optional explicit JDBC URL. If provided, DB adapters may prefer
 *                it over host/port/databaseName.
 * @param connectionMode optional provider-specific mode, for example Oracle
 *                       {@code sid} or {@code service}.
 * @param defaultAccess default access profile alias.
 * @param connectionTimeoutSeconds default connection timeout in seconds.
 * @param queryTimeoutSeconds default query timeout in seconds.
 * @param pool optional database pool settings for future pooled DB clients.
 */
public record DatabaseResourceConfig(
        String type,
        String host,
        Integer port,
        String databaseName,
        String schema,
        String jdbcUrl,
        String connectionMode,
        String defaultAccess,
        Integer connectionTimeoutSeconds,
        Integer queryTimeoutSeconds,
        DatabasePoolConfig pool
) {
}
