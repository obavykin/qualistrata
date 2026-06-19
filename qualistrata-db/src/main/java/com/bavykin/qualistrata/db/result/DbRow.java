package com.bavykin.qualistrata.db.result;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One database result row exposed through typed accessors.
 *
 * <p>Implementations should preferably support case-insensitive column lookup,
 * because different JDBC drivers may expose column labels in different case.</p>
 */
public interface DbRow {

    /**
     * Returns a raw column value.
     *
     * @param column column name or label.
     * @return raw value, or {@code null} when SQL value is NULL.
     */
    Object get(String column);

    /**
     * Returns a column value as a string.
     *
     * @param column column name or label.
     * @return string value, or {@code null} when SQL value is NULL.
     */
    String getString(String column);

    /**
     * Returns a column value as an integer.
     *
     * @param column column name or label.
     * @return integer value, or {@code null} when SQL value is NULL.
     */
    Integer getInteger(String column);

    /**
     * Returns a column value as a long.
     *
     * @param column column name or label.
     * @return long value, or {@code null} when SQL value is NULL.
     */
    Long getLong(String column);

    /**
     * Returns a column value as a boolean.
     *
     * @param column column name or label.
     * @return boolean value, or {@code null} when SQL value is NULL.
     */
    Boolean getBoolean(String column);

    /**
     * Returns a column value as a local date.
     *
     * @param column column name or label.
     * @return local date value, or {@code null} when SQL value is NULL.
     */
    LocalDate getLocalDate(String column);

    /**
     * Returns a column value as a local date and time.
     *
     * @param column column name or label.
     * @return local date-time value, or {@code null} when SQL value is NULL.
     */
    LocalDateTime getLocalDateTime(String column);
}
