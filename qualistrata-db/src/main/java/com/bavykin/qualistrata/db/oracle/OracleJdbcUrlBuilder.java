package com.bavykin.qualistrata.db.oracle;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;

/**
 * Builds Oracle JDBC URLs from the generic QualiStrata DB connection model.
 *
 * <p>The generic {@link DbConnectionConfig} intentionally does not expose
 * Oracle-specific fields such as SID or service name as separate core concepts.
 * The provider therefore interprets {@link DbConnectionConfig#database()} as the
 * Oracle service name by default, and as SID only when the optional provider
 * property {@value #CONNECTION_MODE_PROPERTY} is set to {@value #SID_MODE}.</p>
 */
public final class OracleJdbcUrlBuilder {

    /** Provider-specific property used to select Oracle service or SID URL mode. */
    public static final String CONNECTION_MODE_PROPERTY = "oracle.connectionMode";

    /** Oracle URL mode for service-name based connections. */
    public static final String SERVICE_MODE = "service";

    /** Oracle URL mode for legacy SID-based connections. */
    public static final String SID_MODE = "sid";

    private static final int DEFAULT_ORACLE_PORT = 1521;

    private OracleJdbcUrlBuilder() {
        // Utility class.
    }

    /**
     * Builds an Oracle JDBC URL from host, port, database/service, and optional
     * provider properties.
     *
     * @param config generic DB connection configuration.
     * @return Oracle JDBC URL.
     */
    public static String build(DbConnectionConfig config) {
        validateConfig(config);

        String host = requireText(config.host(), config, "Oracle host must not be blank when JDBC URL is not provided.");
        String database = requireText(config.database(), config, "Oracle database/service/SID must not be blank when JDBC URL is not provided.");
        int port = config.port() == null ? DEFAULT_ORACLE_PORT : config.port();
        validatePort(port, config);

        String mode = config.properties().getOrDefault(CONNECTION_MODE_PROPERTY, SERVICE_MODE);
        if (SERVICE_MODE.equalsIgnoreCase(mode)) {
            return "jdbc:oracle:thin:@//" + host + ":" + port + "/" + database;
        }
        if (SID_MODE.equalsIgnoreCase(mode)) {
            return "jdbc:oracle:thin:@" + host + ":" + port + ":" + database;
        }

        throw new DbException(
                config.alias(),
                config.type(),
                "Unsupported Oracle connection mode: " + mode
                        + ". Expected '" + SERVICE_MODE + "' or '" + SID_MODE + "'."
        );
    }

    private static void validateConfig(DbConnectionConfig config) {
        if (config == null) {
            throw new DbException("Oracle DB connection configuration must not be null.");
        }
        if (config.type() != DbType.ORACLE) {
            throw new DbException(config.alias(), config.type(), "Oracle provider requires DbType.ORACLE configuration.");
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
            throw new DbException(config.alias(), config.type(), "Oracle port must be between 1 and 65535.");
        }
    }
}
