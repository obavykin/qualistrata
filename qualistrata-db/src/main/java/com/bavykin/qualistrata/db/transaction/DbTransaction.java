package com.bavykin.qualistrata.db.transaction;

import com.bavykin.qualistrata.db.result.DbRows;

/**
 * Database operations available inside a transaction boundary.
 *
 * <p>This interface mirrors the regular client operations but intentionally does
 * not expose {@code close()} or nested transaction control to consumer tests.</p>
 */
public interface DbTransaction {

    DbRows query(String sql, Object... parameters);

    int update(String sql, Object... parameters);

    <T> T queryValue(String sql, Class<T> type, Object... parameters);

    boolean exists(String sql, Object... parameters);
}
