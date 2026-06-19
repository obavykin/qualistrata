package com.bavykin.qualistrata.db.postgres;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.client.DbClient;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.jdbc.JdbcDbClient;
import com.bavykin.qualistrata.db.provider.DbProvider;

/**
 * PostgreSQL database provider for the generic QualiStrata DB API.
 *
 * <p>This provider hides PostgreSQL-specific URL construction and SQL snippets
 * behind the common {@link DbProvider} contract. Consumer tests should continue
 * using only {@link DbClient} and other generic DB abstractions.</p>
 */
public class PostgresDbProvider implements DbProvider {

    /**
     * Creates a default PostgreSQL database provider.
     *
     * <p>The provider is stateless. A single instance can be safely reused by
     * the provider registry and by multiple tests.</p>
     */
    public PostgresDbProvider() {
        // Default constructor is declared explicitly to keep generated Javadoc clean.
    }

    private static final String HEALTH_CHECK_SQL = "SELECT 1";

    private static final String DATABASE_VERSION_SQL = "SHOW server_version";

    @Override
    public DbType type() {
        return DbType.POSTGRES;
    }

    @Override
    public DbClient createClient(DbConnectionConfig config) {
        validateConfig(config);
        String jdbcUrl = config.hasExplicitJdbcUrl()
                ? config.jdbcUrl()
                : PostgresJdbcUrlBuilder.build(config);
        return new JdbcDbClient(config, jdbcUrl);
    }

    @Override
    public String defaultHealthCheckSql() {
        return HEALTH_CHECK_SQL;
    }

    @Override
    public String databaseVersionSql() {
        return DATABASE_VERSION_SQL;
    }

    private void validateConfig(DbConnectionConfig config) {
        if (config == null) {
            throw new DbException("PostgreSQL DB connection configuration must not be null.");
        }
        if (config.type() != DbType.POSTGRES) {
            throw new DbException(config.alias(), config.type(), "PostgreSQL provider requires DbType.POSTGRES configuration.");
        }
    }
}
