package com.bavykin.qualistrata.db.jdbc;

import com.bavykin.qualistrata.db.client.DbClient;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.result.DbRows;
import com.bavykin.qualistrata.db.transaction.DbTransaction;
import com.bavykin.qualistrata.db.transaction.DbTransactionCallback;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Generic JDBC implementation of the QualiStrata DB client.
 *
 * <p>This class is database-engine-neutral. It does not know how to build
 * Oracle, PostgreSQL, or any other engine-specific JDBC URL. Providers are
 * responsible for creating the final JDBC URL and then constructing this client.</p>
 *
 * <p>The client owns one JDBC connection and reuses it for multiple operations.
 * This follows the framework principle of avoiding connect/execute/disconnect
 * per command. Connection pooling can be added later if a real need appears.</p>
 */
public class JdbcDbClient implements DbClient {

    private final DbConnectionConfig config;
    private final Connection connection;
    private final JdbcOperations operations;

    private boolean closed;

    /**
     * Opens a JDBC connection using the explicit JDBC URL from the supplied
     * configuration.
     *
     * @param config resolved DB connection configuration.
     */
    public JdbcDbClient(DbConnectionConfig config) {
        this(config, requireExplicitJdbcUrl(config));
    }

    /**
     * Opens a JDBC connection using a provider-built JDBC URL.
     *
     * @param config resolved DB connection configuration.
     * @param jdbcUrl final JDBC URL prepared by an engine-specific provider.
     */
    public JdbcDbClient(DbConnectionConfig config, String jdbcUrl) {
        this.config = requireConfig(config);
        this.connection = openConnection(this.config, jdbcUrl);
        this.operations = new JdbcOperations(this.config, connection);
    }

    @Override
    public DbRows query(String sql, Object... parameters) {
        ensureOpen();
        return operations.query(sql, parameters);
    }

    @Override
    public int update(String sql, Object... parameters) {
        ensureOpen();
        return operations.update(sql, parameters);
    }

    @Override
    public <T> T queryValue(String sql, Class<T> type, Object... parameters) {
        ensureOpen();
        return operations.queryValue(sql, type, parameters);
    }

    @Override
    public boolean exists(String sql, Object... parameters) {
        ensureOpen();
        return operations.exists(sql, parameters);
    }

    @Override
    public void transaction(DbTransactionCallback callback) {
        ensureOpen();
        if (callback == null) {
            throw new DbException(config.alias(), config.type(), "DB transaction callback must not be null.");
        }

        boolean previousAutoCommit = readAutoCommit();
        try {
            connection.setAutoCommit(false);
            DbTransaction transaction = new JdbcDbTransaction(config, connection);
            callback.execute(transaction);
            connection.commit();
        } catch (RuntimeException exception) {
            rollbackAfterFailure(exception);
            throw exception;
        } catch (SQLException exception) {
            rollbackAfterFailure(exception);
            throw new DbException(config.alias(), config.type(), "DB transaction failed.", exception);
        } finally {
            restoreAutoCommit(previousAutoCommit);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        try {
            connection.close();
            closed = true;
        } catch (SQLException exception) {
            throw new DbException(config.alias(), config.type(), "Failed to close DB connection.", exception);
        }
    }

    private boolean readAutoCommit() {
        try {
            return connection.getAutoCommit();
        } catch (SQLException exception) {
            throw new DbException(config.alias(), config.type(), "Failed to read DB auto-commit mode.", exception);
        }
    }

    private void rollbackAfterFailure(Exception originalFailure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            originalFailure.addSuppressed(rollbackFailure);
        }
    }

    private void restoreAutoCommit(boolean previousAutoCommit) {
        try {
            connection.setAutoCommit(previousAutoCommit);
        } catch (SQLException exception) {
            throw new DbException(config.alias(), config.type(), "Failed to restore DB auto-commit mode.", exception);
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new DbException(config.alias(), config.type(), "DB client is already closed.");
        }
    }

    private static DbConnectionConfig requireConfig(DbConnectionConfig config) {
        if (config == null) {
            throw new DbException("DB connection configuration must not be null.");
        }
        return config;
    }

    private static String requireExplicitJdbcUrl(DbConnectionConfig config) {
        DbConnectionConfig safeConfig = requireConfig(config);
        if (!safeConfig.hasExplicitJdbcUrl()) {
            throw new DbException(
                    safeConfig.alias(),
                    safeConfig.type(),
                    "Explicit JDBC URL is required when JdbcDbClient is created without a provider-built URL."
            );
        }
        return safeConfig.jdbcUrl();
    }

    private static Connection openConnection(DbConnectionConfig config, String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new DbException(config.alias(), config.type(), "JDBC URL must not be blank.");
        }

        try {
            return DriverManager.getConnection(jdbcUrl, connectionProperties(config));
        } catch (SQLException exception) {
            throw new DbException(
                    config.alias(),
                    config.type(),
                    "Failed to open DB connection. alias=" + config.alias()
                            + ", type=" + config.type()
                            + ", jdbcUrl=<configured>"
                            + ", exceptionType=" + exception.getClass().getName()
                            + ", safeMessage=" + exception.getMessage(),
                    exception
            );
        }
    }

    private static Properties connectionProperties(DbConnectionConfig config) {
        Properties properties = new Properties();
        config.properties().forEach(properties::setProperty);
        if (config.username() != null) {
            properties.setProperty("user", config.username());
        }
        if (config.password() != null) {
            properties.setProperty("password", config.password());
        }
        return properties;
    }
}
