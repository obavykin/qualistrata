package com.bavykin.qualistrata.db.jdbc;

import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.result.DbRow;
import com.bavykin.qualistrata.db.result.DbRows;

import java.util.List;

/**
 * Immutable materialized JDBC query result.
 *
 * <p>The first implementation keeps rows fully materialized because automated
 * verification queries should be bounded and focused. Streaming can be added
 * later if a real performance need appears.</p>
 */
public class JdbcDbRows implements DbRows {

    private final List<DbRow> rows;

    /**
     * Creates a materialized query result.
     *
     * @param rows result rows in JDBC order.
     */
    public JdbcDbRows(List<? extends DbRow> rows) {
        if (rows == null) {
            throw new DbException("DB rows collection must not be null.");
        }
        this.rows = List.copyOf(rows);
    }

    @Override
    public int size() {
        return rows.size();
    }

    @Override
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @Override
    public DbRow single() {
        if (rows.size() != 1) {
            throw new DbException("Expected exactly one DB row, but got: " + rows.size());
        }
        return rows.get(0);
    }

    @Override
    public List<DbRow> all() {
        return rows;
    }
}
