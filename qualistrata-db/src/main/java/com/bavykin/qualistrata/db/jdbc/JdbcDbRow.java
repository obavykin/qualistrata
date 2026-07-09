package com.bavykin.qualistrata.db.jdbc;

import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.result.DbRow;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * JDBC-backed immutable implementation of one database result row.
 *
 * <p>The row stores values by JDBC column label and resolves columns in a
 * case-insensitive way. This is important because different database engines and
 * JDBC drivers may expose column labels using different case conventions.</p>
 */
class JdbcDbRow implements DbRow {

    private final Map<String, Object> valuesByOriginalColumn;
    private final Map<String, String> originalColumnByNormalizedColumn;

    /**
     * Creates a row from already materialized column values.
     *
     * <p>The map order is preserved so diagnostics and future rendering can show
     * columns in the same order as they were returned by the JDBC driver.</p>
     *
     * @param valuesByColumn values keyed by JDBC column label.
     */
    JdbcDbRow(Map<String, Object> valuesByColumn) {
        if (valuesByColumn == null) {
            throw new DbException("DB row values must not be null.");
        }

        this.valuesByOriginalColumn = Collections.unmodifiableMap(new LinkedHashMap<>(valuesByColumn));
        this.originalColumnByNormalizedColumn = buildNormalizedIndex(valuesByColumn);
    }

    @Override
    public Object get(String column) {
        String originalColumn = originalColumnByNormalizedColumn.get(normalize(column));
        if (originalColumn == null) {
            throw new DbException("Column not found in DB row: " + column);
        }
        return valuesByOriginalColumn.get(originalColumn);
    }

    @Override
    public String getString(String column) {
        Object value = get(column);
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public Integer getInteger(String column) {
        Object value = get(column);
        return switch (value) {
            case null -> null;
            case Number number -> number.intValue();
            case String text -> Integer.valueOf(text);
            default -> throw unsupportedConversion(column, value, "Integer");
        };
    }

    @Override
    public Long getLong(String column) {
        Object value = get(column);
        return switch (value) {
            case null -> null;
            case Number number -> number.longValue();
            case String text -> Long.valueOf(text);
            default -> throw unsupportedConversion(column, value, "Long");
        };
    }

    @Override
    public Boolean getBoolean(String column) {
        Object value = get(column);
        return switch (value) {
            case null -> null;
            case Boolean bool -> bool;
            case Number number -> number.intValue() != 0;
            case String text -> Boolean.valueOf(text);
            default -> throw unsupportedConversion(column, value, "Boolean");
        };
    }

    @Override
    public LocalDate getLocalDate(String column) {
        Object value = get(column);
        return switch (value) {
            case null -> null;
            case LocalDate localDate -> localDate;
            case Date sqlDate -> sqlDate.toLocalDate();
            case Timestamp timestamp -> timestamp.toLocalDateTime().toLocalDate();
            case LocalDateTime localDateTime -> localDateTime.toLocalDate();
            default -> throw unsupportedConversion(column, value, "LocalDate");
        };
    }

    @Override
    public LocalDateTime getLocalDateTime(String column) {
        Object value = get(column);
        return switch (value) {
            case null -> null;
            case LocalDateTime localDateTime -> localDateTime;
            case Timestamp timestamp -> timestamp.toLocalDateTime();
            case Date sqlDate -> sqlDate.toLocalDate().atStartOfDay();
            case LocalDate localDate -> localDate.atStartOfDay();
            default -> throw unsupportedConversion(column, value, "LocalDateTime");
        };
    }

    /**
     * Returns the first column value according to JDBC result-set order.
     *
     * <p>This method is package-private because it is an implementation detail
     * used by {@link JdbcDbClient#queryValue(String, Class, Object...)}.</p>
     *
     * @return first column value.
     */
    Object firstValue() {
        if (valuesByOriginalColumn.isEmpty()) {
            throw new DbException("DB row does not contain any columns.");
        }
        return valuesByOriginalColumn.values().iterator().next();
    }

    private static Map<String, String> buildNormalizedIndex(Map<String, Object> valuesByColumn) {
        Map<String, String> index = new LinkedHashMap<>();
        for (String column : valuesByColumn.keySet()) {
            if (column == null || column.isBlank()) {
                throw new DbException("DB row column label must not be blank.");
            }
            index.put(normalize(column), column);
        }
        return Collections.unmodifiableMap(index);
    }

    private static String normalize(String column) {
        if (column == null || column.isBlank()) {
            throw new DbException("DB row column name must not be blank.");
        }
        return column.toLowerCase(Locale.ROOT);
    }

    private static DbException unsupportedConversion(String column, Object value, String targetType) {
        return new DbException("Cannot convert DB column '" + column + "' value of type "
                + value.getClass().getName() + " to " + targetType + ".");
    }
}
