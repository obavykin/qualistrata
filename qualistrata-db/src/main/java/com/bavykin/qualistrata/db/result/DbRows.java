package com.bavykin.qualistrata.db.result;

import java.util.List;

/**
 * Materialized result of a database query.
 *
 * <p>The first implementation can keep results fully materialized because test
 * automation queries should usually be focused and bounded. Streaming APIs can
 * be introduced later if a real need appears.</p>
 */
public interface DbRows {

    /**
     * Returns the number of rows in the query result.
     *
     * @return number of rows in the result.
     */
    int size();

    /**
     * Indicates whether the query result contains no rows.
     *
     * @return {@code true} when the result has no rows.
     */
    boolean isEmpty();

    /**
     * Returns the only row in the result.
     *
     * <p>Implementations should fail fast when the result contains zero rows or
     * more than one row, because such situations usually indicate an incorrect
     * verification query in a test.</p>
     *
     * @return single row.
     */
    DbRow single();

    /**
     * Returns all result rows as an immutable list.
     *
     * @return immutable list of all result rows.
     */
    List<DbRow> all();
}
