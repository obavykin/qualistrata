package com.bavykin.qualistrata.db.client;

import com.bavykin.qualistrata.db.result.DbRows;
import com.bavykin.qualistrata.db.transaction.DbTransactionCallback;

/**
 * Generic database client used by consumer tests.
 *
 * <p>This contract hides JDBC boilerplate from UI, API, performance, smoke,
 * integration, and regression tests. It intentionally contains only database-
 * neutral operations. Oracle/PostgreSQL-specific behavior belongs to providers
 * and JDBC implementations behind this interface.</p>
 */
public interface DbClient extends AutoCloseable {

    /**
     * Executes a SELECT-like query and returns all rows materialized into a
     * framework result model.
     *
     * @param sql SQL text with positional placeholders.
     * @param parameters positional parameter values.
     * @return query result rows.
     */
    DbRows query(String sql, Object... parameters);

    /**
     * Executes INSERT, UPDATE, DELETE, or DDL when a consumer project explicitly
     * decides to use DDL in tests.
     *
     * @param sql SQL text with positional placeholders.
     * @param parameters positional parameter values.
     * @return affected row count reported by the JDBC driver.
     */
    int update(String sql, Object... parameters);

    /**
     * Executes a query expected to return one scalar value.
     *
     * @param sql SQL text with positional placeholders.
     * @param type expected Java type of the scalar value.
     * @param parameters positional parameter values.
     * @param <T> scalar Java type.
     * @return scalar value converted by the implementation.
     */
    <T> T queryValue(String sql, Class<T> type, Object... parameters);

    /**
     * Executes a query and returns whether at least one row exists.
     *
     * @param sql SQL text with positional placeholders.
     * @param parameters positional parameter values.
     * @return {@code true} when the query returns at least one row.
     */
    boolean exists(String sql, Object... parameters);

    /**
     * Executes multiple DB operations inside one transaction boundary.
     *
     * <p>The implementation must commit if the callback completes and roll back
     * if it throws an exception.</p>
     *
     * @param callback transaction body.
     */
    void transaction(DbTransactionCallback callback);

    /**
     * Releases the underlying database resources.
     */
    @Override
    void close();
}
