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

    String getString(String column);

    Integer getInteger(String column);

    Long getLong(String column);

    Boolean getBoolean(String column);

    LocalDate getLocalDate(String column);

    LocalDateTime getLocalDateTime(String column);
}
