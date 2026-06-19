package com.bavykin.qualistrata.db.postgres;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;

/**
 * Builds PostgreSQL JDBC URLs from the generic QualiStrata DB connection model.
 *
 * <p>The builder deliberately supports only the common host/port/database shape
 * for the first provider increment. Additional PostgreSQL-specific options can
 * be supplied through {@link DbConnectionConfig#properties()} and interpreted by
 * the JDBC driver or by a later provider enhancement.</p>
 */
public final class PostgresJdbcUrlBuilder {

    private static final int DEFAULT_POSTGRES_PORT = 5432;

    private PostgresJdbcUrlBuilder() {
        // Utility class.
    }

    /**
     * Builds a PostgreSQL JDBC URL from host, optional port, and database name.
     *
     * @param config generic DB connection configuration.
     * @return PostgreSQL JDBC URL.
     */
    public static String build(DbConnectionConfig config) {
        validateConfig(config);

        String host = requireText(config.host(), config, "PostgreSQL host must not be blank when JDBC URL is not provided.");
        String database = requireText(config.database(), config, "PostgreSQL database must not be blank when JDBC URL is not provided.");
        int port = config.port() == null ? DEFAULT_POSTGRES_PORT : config.port();
        validatePort(port, config);

        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }

    private static void validateConfig(DbConnectionConfig config) {
        if (config == null) {
            throw new DbException("PostgreSQL DB connection configuration must not be null.");
        }
        if (config.type() != DbType.POSTGRES) {
            throw new DbException(config.alias(), config.type(), "PostgreSQL provider requires DbType.POSTGRES configuration.");
        }
    }

    private static String requireText(String value, DbConnectionConfig config, String message) {
        if (value == null || value.isBlank()) {
            throw new DbException(config.alias(), config.type(), message);
        }
        return value;
    }

    private static void validatePort(int port, DbConnectionConfig config) {
        if (port <= 0 || port > 65535) {
            throw new DbException(config.alias(), config.type(), "PostgreSQL port must be between 1 and 65535.");
        }
    }
}
