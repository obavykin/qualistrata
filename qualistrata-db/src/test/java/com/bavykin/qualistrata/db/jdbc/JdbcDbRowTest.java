package com.bavykin.qualistrata.db.jdbc;

import com.bavykin.qualistrata.db.exception.DbException;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JDBC row value access and type conversion.
 *
 * <p>The row implementation must be case-insensitive because Oracle,
 * PostgreSQL, H2, and other JDBC drivers may expose column labels in different
 * case depending on SQL aliases and engine-specific rules.</p>
 */
class JdbcDbRowTest {

    @Test
    void shouldResolveColumnsCaseInsensitively() {
        JdbcDbRow row = new JdbcDbRow(Map.of(
                "ID", 10,
                "display_name", "Alice"
        ));

        assertEquals(10, row.getInteger("id"));
        assertEquals(10L, row.getLong("Id"));
        assertEquals("Alice", row.getString("DISPLAY_NAME"));
    }

    @Test
    void shouldConvertSupportedTypes() {
        LocalDate date = LocalDate.of(2026, 6, 19);
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 19, 12, 30);

        JdbcDbRow row = new JdbcDbRow(Map.of(
                "int_value", 10L,
                "long_value", 20,
                "bool_value", 1,
                "date_value", Date.valueOf(date),
                "timestamp_value", Timestamp.valueOf(dateTime)
        ));

        assertEquals(10, row.getInteger("int_value"));
        assertEquals(20L, row.getLong("long_value"));
        assertTrue(row.getBoolean("bool_value"));
        assertEquals(date, row.getLocalDate("date_value"));
        assertEquals(dateTime, row.getLocalDateTime("timestamp_value"));
    }

    @Test
    void shouldReturnNullForSqlNullValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("nullable", null);

        JdbcDbRow row = new JdbcDbRow(values);

        assertNull(row.get("nullable"));
        assertNull(row.getString("nullable"));
        assertNull(row.getInteger("nullable"));
        assertNull(row.getLong("nullable"));
        assertNull(row.getBoolean("nullable"));
        assertNull(row.getLocalDate("nullable"));
        assertNull(row.getLocalDateTime("nullable"));
    }

    @Test
    void shouldFailWhenColumnDoesNotExist() {
        JdbcDbRow row = new JdbcDbRow(Map.of("id", 1));

        assertThrows(DbException.class, () -> row.get("missing"));
    }

    @Test
    void shouldExposeFirstValueInColumnOrder() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("first", "A");
        values.put("second", "B");

        JdbcDbRow row = new JdbcDbRow(values);

        assertEquals("A", row.firstValue());
    }
}
