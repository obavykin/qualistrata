package com.bavykin.qualistrata.db.transaction;

import com.bavykin.qualistrata.db.result.DbRows;

/**
 * Database operations available inside a transaction boundary.
 *
 * <p>This interface mirrors the regular client operations but intentionally does
 * not expose {@code close()} or nested transaction control to consumer tests.</p>
 */
public interface DbTransaction {

    /**
     * Executes a SELECT-like query inside the current transaction.
     *
     * @param sql SQL text with positional placeholders.
     * @param parameters positional parameter values.
     * @return query result rows.
     */
    DbRows query(String sql, Object... parameters);

    /**
     * Executes INSERT, UPDATE, DELETE, or explicitly requested DDL inside the
     * current transaction.
     *
     * @param sql SQL text with positional placeholders.
     * @param parameters positional parameter values.
     * @return affected row count reported by the JDBC driver.
     */
    int update(String sql, Object... parameters);

    /**
     * Executes a query expected to return one scalar value inside the current
     * transaction.
     *
     * @param sql SQL text with positional placeholders.
     * @param type expected Java type of the scalar value.
     * @param parameters positional parameter values.
     * @param <T> scalar Java type.
     * @return scalar value converted by the implementation.
     */
    <T> T queryValue(String sql, Class<T> type, Object... parameters);

    /**
     * Executes a query inside the current transaction and returns whether at
     * least one row exists.
     *
     * @param sql SQL text with positional placeholders.
     * @param parameters positional parameter values.
     * @return {@code true} when the query returns at least one row.
     */
    boolean exists(String sql, Object... parameters);
}
