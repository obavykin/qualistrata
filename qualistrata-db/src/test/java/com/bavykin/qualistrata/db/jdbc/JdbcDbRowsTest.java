package com.bavykin.qualistrata.db.jdbc;

import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.result.DbRow;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for materialized JDBC result rows.
 *
 * <p>These tests do not use a real database. They verify the framework result
 * model behavior independently from JDBC driver behavior.</p>
 */
class JdbcDbRowsTest {

    @Test
    void shouldReturnSingleRowWhenExactlyOneRowExists() {
        DbRow row = new JdbcDbRow(Map.of("id", 1));
        JdbcDbRows rows = new JdbcDbRows(List.of(row));

        assertEquals(1, rows.size());
        assertFalse(rows.isEmpty());
        assertSame(row, rows.single());
        assertEquals(List.of(row), rows.all());
    }

    @Test
    void shouldFailSingleWhenNoRowsExist() {
        JdbcDbRows rows = new JdbcDbRows(List.of());

        assertTrue(rows.isEmpty());
        assertThrows(DbException.class, rows::single);
    }

    @Test
    void shouldFailSingleWhenMoreThanOneRowExists() {
        JdbcDbRows rows = new JdbcDbRows(List.of(
                new JdbcDbRow(Map.of("id", 1)),
                new JdbcDbRow(Map.of("id", 2))
        ));

        assertEquals(2, rows.size());
        assertThrows(DbException.class, rows::single);
    }

    @Test
    void shouldExposeImmutableRowsList() {
        JdbcDbRows rows = new JdbcDbRows(List.of(new JdbcDbRow(Map.of("id", 1))));

        assertThrows(UnsupportedOperationException.class, () -> rows.all().add(new JdbcDbRow(Map.of("id", 2))));
    }
}
