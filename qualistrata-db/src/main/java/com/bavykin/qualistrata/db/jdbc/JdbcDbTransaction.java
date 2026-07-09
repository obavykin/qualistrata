package com.bavykin.qualistrata.db.jdbc;

import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.result.DbRows;
import com.bavykin.qualistrata.db.transaction.DbTransaction;

import java.sql.Connection;

/**
 * Transaction-scoped JDBC DB operations.
 *
 * <p>This class intentionally does not own or close the JDBC connection. The
 * surrounding {@link JdbcDbClient} controls transaction boundaries, commit,
 * rollback, and resource lifecycle.</p>
 */
class JdbcDbTransaction implements DbTransaction {

    private final JdbcOperations operations;

    JdbcDbTransaction(DbConnectionConfig config, Connection connection) {
        this.operations = new JdbcOperations(config, connection);
    }

    @Override
    public DbRows query(String sql, Object... parameters) {
        return operations.query(sql, parameters);
    }

    @Override
    public int update(String sql, Object... parameters) {
        return operations.update(sql, parameters);
    }

    @Override
    public <T> T queryValue(String sql, Class<T> type, Object... parameters) {
        return operations.queryValue(sql, type, parameters);
    }

    @Override
    public boolean exists(String sql, Object... parameters) {
        return operations.exists(sql, parameters);
    }
}
