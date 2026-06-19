package com.bavykin.qualistrata.db.oracle;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.client.DbClient;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.jdbc.JdbcDbClient;
import com.bavykin.qualistrata.db.provider.DbProvider;

/**
 * Oracle database provider for the generic QualiStrata DB API.
 *
 * <p>This provider keeps Oracle-specific behavior outside the core API: JDBC URL
 * construction, health-check SQL, and database-version SQL are all isolated in
 * the Oracle package. Consumer tests still use the generic {@link DbClient}
 * contract.</p>
 */
public class OracleDbProvider implements DbProvider {

    /**
     * Creates a default Oracle database provider.
     *
     * <p>The provider is stateless. A single instance can be safely reused by
     * the provider registry and by multiple tests.</p>
     */
    public OracleDbProvider() {
        // Default constructor is declared explicitly to keep generated Javadoc clean.
    }

    private static final String HEALTH_CHECK_SQL = "SELECT 1 FROM dual";

    private static final String DATABASE_VERSION_SQL = """
            SELECT version_full
            FROM product_component_version
            WHERE product LIKE 'Oracle Database%'
            """;

    @Override
    public DbType type() {
        return DbType.ORACLE;
    }

    @Override
    public DbClient createClient(DbConnectionConfig config) {
        validateConfig(config);
        String jdbcUrl = config.hasExplicitJdbcUrl()
                ? config.jdbcUrl()
                : OracleJdbcUrlBuilder.build(config);
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
            throw new DbException("Oracle DB connection configuration must not be null.");
        }
        if (config.type() != DbType.ORACLE) {
            throw new DbException(config.alias(), config.type(), "Oracle provider requires DbType.ORACLE configuration.");
        }
    }
}
