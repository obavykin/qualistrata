package com.bavykin.qualistrata.db.config;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.exception.DbException;

import java.time.Duration;
import java.util.Map;

/**
 * Immutable runtime configuration for one logical database connection.
 *
 * <p>The configuration is generic and must not contain Oracle-only assumptions.
 * A consumer project may provide a ready JDBC URL, or it may provide host, port,
 * and database/service information so an engine-specific provider can build the
 * correct JDBC URL later.</p>
 *
 * <p>Secrets must be passed into this object only after they have been resolved
 * by the project configuration/secrets layer. This record intentionally avoids
 * printing the password in {@link #toString()}.</p>
 *
 * @param alias logical connection alias used by tests and diagnostics.
 * @param type database engine type used to resolve a provider.
 * @param host database host, used when {@code jdbcUrl} is not supplied.
 * @param port database port, used when {@code jdbcUrl} is not supplied.
 * @param database database name, service name, or SID depending on provider rules.
 * @param schema optional default schema.
 * @param username database username.
 * @param password database password; must never be logged.
 * @param jdbcUrl explicit JDBC URL; if present, provider should prefer it.
 * @param connectionTimeout connection timeout for client creation/validation.
 * @param queryTimeout default query timeout.
 * @param properties optional provider-specific connection properties.
 */
public record DbConnectionConfig(
        String alias,
        DbType type,
        String host,
        Integer port,
        String database,
        String schema,
        String username,
        String password,
        String jdbcUrl,
        Duration connectionTimeout,
        Duration queryTimeout,
        Map<String, String> properties
) {

    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_QUERY_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Normalizes and validates the immutable DB connection configuration.
     *
     * <p>The constructor applies safe defaults for timeouts and copies the
     * provider-specific properties map so callers cannot mutate configuration
     * after the record has been created.</p>
     */
    public DbConnectionConfig {
        alias = requireText(alias, "Database connection alias must not be blank.");
        if (type == null) {
            throw new DbException(alias, null, "Database type must not be null.");
        }

        connectionTimeout = defaultIfNull(connectionTimeout, DEFAULT_CONNECTION_TIMEOUT);
        queryTimeout = defaultIfNull(queryTimeout, DEFAULT_QUERY_TIMEOUT);
        validatePositive(connectionTimeout, alias, "Connection timeout must be positive.");
        validatePositive(queryTimeout, alias, "Query timeout must be positive.");

        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    /**
     * Indicates whether a provider should use the explicit JDBC URL instead of
     * constructing one from host/port/database fields.
     *
     * @return {@code true} when an explicit JDBC URL was configured.
     */
    public boolean hasExplicitJdbcUrl() {
        return jdbcUrl != null && !jdbcUrl.isBlank();
    }

    /**
     * Creates a safe diagnostic string without password or secret values.
     *
     * @return masked configuration summary suitable for logs and reports.
     */
    @Override
    public String toString() {
        return "DbConnectionConfig[" +
                "alias='" + alias + '\'' +
                ", type=" + type +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", database='" + database + '\'' +
                ", schema='" + schema + '\'' +
                ", username='" + username + '\'' +
                ", password=<masked>" +
                ", jdbcUrl=" + maskJdbcUrl(jdbcUrl) +
                ", connectionTimeout=" + connectionTimeout +
                ", queryTimeout=" + queryTimeout +
                ", properties=" + properties.keySet() +
                ']';
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DbException(null, null, message);
        }
        return value;
    }

    private static Duration defaultIfNull(Duration value, Duration defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static void validatePositive(Duration value, String alias, String message) {
        if (value.isZero() || value.isNegative()) {
            throw new DbException(alias, null, message);
        }
    }

    private static String maskJdbcUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "<configured>";
    }
}
