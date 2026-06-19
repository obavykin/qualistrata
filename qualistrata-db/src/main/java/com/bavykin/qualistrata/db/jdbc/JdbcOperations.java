package com.bavykin.qualistrata.db.jdbc;

import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.result.DbRow;
import com.bavykin.qualistrata.db.result.DbRows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared JDBC operation executor used by both the regular client and the
 * transaction-scoped client.
 *
 * <p>The class is package-private because it is an implementation detail of the
 * JDBC layer. It centralizes parameter binding, timeout handling, result-set
 * materialization, scalar extraction, and safe exception wrapping.</p>
 */
class JdbcOperations {

    private final DbConnectionConfig config;
    private final Connection connection;

    JdbcOperations(DbConnectionConfig config, Connection connection) {
        this.config = config;
        this.connection = connection;
    }

    DbRows query(String sql, Object... parameters) {
        validateSql(sql);
        Object[] safeParameters = normalizeParameters(parameters);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            configure(statement, safeParameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRows(resultSet);
            }
        } catch (SQLException exception) {
            throw wrap("DB query failed.", sql, safeParameters.length, exception);
        }
    }

    int update(String sql, Object... parameters) {
        validateSql(sql);
        Object[] safeParameters = normalizeParameters(parameters);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            configure(statement, safeParameters);
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw wrap("DB update failed.", sql, safeParameters.length, exception);
        }
    }

    <T> T queryValue(String sql, Class<T> type, Object... parameters) {
        if (type == null) {
            throw new DbException(config.alias(), config.type(), "Scalar query target type must not be null.");
        }

        DbRow row = query(sql, parameters).single();
        Object value = ((JdbcDbRow) row).firstValue();
        return convertScalar(value, type, sql);
    }

    boolean exists(String sql, Object... parameters) {
        return !query(sql, parameters).isEmpty();
    }

    private void configure(PreparedStatement statement, Object[] parameters) throws SQLException {
        applyQueryTimeout(statement, config.queryTimeout());
        bindParameters(statement, parameters);
    }

    private void applyQueryTimeout(Statement statement, Duration timeout) throws SQLException {
        if (timeout == null) {
            return;
        }
        long seconds = Math.max(1L, timeout.toSeconds());
        if (seconds > Integer.MAX_VALUE) {
            seconds = Integer.MAX_VALUE;
        }
        statement.setQueryTimeout((int) seconds);
    }

    private void bindParameters(PreparedStatement statement, Object[] parameters) throws SQLException {
        for (int index = 0; index < parameters.length; index++) {
            statement.setObject(index + 1, parameters[index]);
        }
    }

    private DbRows mapRows(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        List<DbRow> rows = new ArrayList<>();

        while (resultSet.next()) {
            Map<String, Object> values = new LinkedHashMap<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                String label = columnLabel(metadata, columnIndex);
                values.put(label, resultSet.getObject(columnIndex));
            }
            rows.add(new JdbcDbRow(values));
        }

        return new JdbcDbRows(rows);
    }

    private String columnLabel(ResultSetMetaData metadata, int columnIndex) throws SQLException {
        String label = metadata.getColumnLabel(columnIndex);
        if (label == null || label.isBlank()) {
            label = metadata.getColumnName(columnIndex);
        }
        if (label == null || label.isBlank()) {
            throw new DbException(config.alias(), config.type(), "JDBC driver returned a blank column label.");
        }
        return label;
    }

    private <T> T convertScalar(Object value, Class<T> type, String sql) {
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        if (type == String.class) {
            return type.cast(String.valueOf(value));
        }
        if (type == Integer.class && value instanceof Number number) {
            return type.cast(number.intValue());
        }
        if (type == Long.class && value instanceof Number number) {
            return type.cast(number.longValue());
        }
        if (type == Boolean.class && value instanceof Boolean bool) {
            return type.cast(bool);
        }
        throw new DbException(
                config.alias(),
                config.type(),
                "Cannot convert scalar DB value of type " + value.getClass().getName()
                        + " to " + type.getName() + ". sql=" + sql
        );
    }

    private void validateSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new DbException(config.alias(), config.type(), "SQL must not be blank.");
        }
    }

    private Object[] normalizeParameters(Object[] parameters) {
        return parameters == null ? new Object[0] : parameters;
    }

    private DbException wrap(String message, String sql, int parameterCount, SQLException exception) {
        return new DbException(
                config.alias(),
                config.type(),
                message + " alias=" + config.alias()
                        + ", type=" + config.type()
                        + ", sql=" + sql
                        + ", parameterCount=" + parameterCount
                        + ", exceptionType=" + exception.getClass().getName()
                        + ", safeMessage=" + exception.getMessage(),
                exception
        );
    }
}
