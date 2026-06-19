package com.bavykin.qualistrata.db.jdbc;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.result.DbRow;
import com.bavykin.qualistrata.db.result.DbRows;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generic JDBC DB client.
 *
 * <p>These tests use H2 as an in-memory JDBC driver only to verify generic JDBC
 * behavior. H2 is not introduced as a production-supported engine in this
 * increment. Real Oracle/PostgreSQL support will be provided through dedicated
 * providers in later increments.</p>
 */
class JdbcDbClientTest {

    @Test
    void shouldExecuteUpdateAndQuery() {
        try (JdbcDbClient client = newClient()) {
            createTable(client);

            int inserted = client.update(
                    "INSERT INTO sample_entity(id, name, active) VALUES (?, ?, ?)",
                    1,
                    "Alice",
                    true
            );

            DbRows rows = client.query("SELECT id AS ID, name AS display_name, active FROM sample_entity WHERE id = ?", 1);
            DbRow row = rows.single();

            assertEquals(1, inserted);
            assertEquals(1, rows.size());
            assertEquals(1, row.getInteger("id"));
            assertEquals("Alice", row.getString("DISPLAY_NAME"));
            assertTrue(row.getBoolean("active"));
        }
    }

    @Test
    void shouldExecuteScalarQuery() {
        try (JdbcDbClient client = newClient()) {
            createTable(client);
            client.update("INSERT INTO sample_entity(id, name, active) VALUES (?, ?, ?)", 1, "Alice", true);
            client.update("INSERT INTO sample_entity(id, name, active) VALUES (?, ?, ?)", 2, "Bob", false);

            Integer count = client.queryValue("SELECT COUNT(*) FROM sample_entity", Integer.class);
            String name = client.queryValue("SELECT name FROM sample_entity WHERE id = ?", String.class, 1);

            assertEquals(2, count);
            assertEquals("Alice", name);
        }
    }

    @Test
    void shouldCheckWhetherRowsExist() {
        try (JdbcDbClient client = newClient()) {
            createTable(client);
            client.update("INSERT INTO sample_entity(id, name, active) VALUES (?, ?, ?)", 1, "Alice", true);

            assertTrue(client.exists("SELECT 1 FROM sample_entity WHERE name = ?", "Alice"));
            assertFalse(client.exists("SELECT 1 FROM sample_entity WHERE name = ?", "Missing"));
        }
    }

    @Test
    void shouldCommitTransactionWhenCallbackCompletes() {
        try (JdbcDbClient client = newClient()) {
            createTable(client);

            client.transaction(tx -> tx.update(
                    "INSERT INTO sample_entity(id, name, active) VALUES (?, ?, ?)",
                    1,
                    "Committed",
                    true
            ));

            assertTrue(client.exists("SELECT 1 FROM sample_entity WHERE name = ?", "Committed"));
        }
    }

    @Test
    void shouldRollbackTransactionWhenCallbackFails() {
        try (JdbcDbClient client = newClient()) {
            createTable(client);

            RuntimeException failure = assertThrows(RuntimeException.class, () -> client.transaction(tx -> {
                tx.update(
                        "INSERT INTO sample_entity(id, name, active) VALUES (?, ?, ?)",
                        1,
                        "Rolled back",
                        true
                );
                throw new RuntimeException("Simulated test failure");
            }));

            assertEquals("Simulated test failure", failure.getMessage());
            assertFalse(client.exists("SELECT 1 FROM sample_entity WHERE name = ?", "Rolled back"));
        }
    }

    @Test
    void shouldWrapSqlFailuresWithSafeDiagnostics() {
        try (JdbcDbClient client = newClient()) {
            DbException exception = assertThrows(
                    DbException.class,
                    () -> client.query("SELECT * FROM missing_table WHERE password = ?", "super-secret")
            );

            assertEquals("test-db", exception.dbAlias());
            assertEquals(DbType.POSTGRES, exception.dbType());
            assertTrue(exception.getMessage().contains("parameterCount=1"));
            assertFalse(exception.getMessage().contains("super-secret"));
        }
    }


    @Test
    void shouldPreserveOriginalTransactionFailureWhenAutoCommitRestoreFails() {
        JdbcDbClient client = new JdbcDbClient(defaultConfig(), connectionThatFailsWhenAutoCommitIsRestored());

        RuntimeException failure = assertThrows(
                RuntimeException.class,
                () -> client.transaction(tx -> {
                    throw new RuntimeException("Original transaction failure");
                })
        );

        assertEquals("Original transaction failure", failure.getMessage());
        assertEquals(1, failure.getSuppressed().length);
        assertInstanceOf(DbException.class, failure.getSuppressed()[0]);
        assertTrue(failure.getSuppressed()[0].getMessage().contains("Failed to restore DB auto-commit mode."));
    }

    @Test
    void shouldRejectOperationsAfterClose() {
        JdbcDbClient client = newClient();
        client.close();

        assertThrows(DbException.class, () -> client.query("SELECT 1"));
    }

    private JdbcDbClient newClient() {
        String databaseName = "qualistrata_" + UUID.randomUUID().toString().replace('-', '_');
        DbConnectionConfig config = new DbConnectionConfig(
                "test-db",
                DbType.POSTGRES,
                null,
                null,
                databaseName,
                null,
                "sa",
                "",
                "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1",
                Duration.ofSeconds(5),
                Duration.ofSeconds(5),
                Map.of()
        );
        return new JdbcDbClient(config);
    }

    private DbConnectionConfig defaultConfig() {
        return new DbConnectionConfig(
                "test-db",
                DbType.POSTGRES,
                null,
                null,
                "test",
                null,
                "sa",
                "",
                "jdbc:h2:mem:test",
                Duration.ofSeconds(5),
                Duration.ofSeconds(5),
                Map.of()
        );
    }

    private Connection connectionThatFailsWhenAutoCommitIsRestored() {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> handleConnectionMethod(method.getName(), args)
        );
    }

    private Object handleConnectionMethod(String methodName, Object[] args) throws SQLException {
        return switch (methodName) {
            case "getAutoCommit" -> true;
            case "setAutoCommit" -> {
                boolean requestedAutoCommit = (Boolean) args[0];
                if (requestedAutoCommit) {
                    throw new SQLException("Simulated restore failure");
                }
                yield null;
            }
            case "rollback", "commit", "close" -> null;
            case "isClosed" -> false;
            case "toString" -> "connectionThatFailsWhenAutoCommitIsRestored";
            default -> throw new UnsupportedOperationException("Unexpected Connection method: " + methodName);
        };
    }

    private void createTable(JdbcDbClient client) {
        client.update("""
                CREATE TABLE sample_entity(
                    id INT PRIMARY KEY,
                    name VARCHAR(100),
                    active BOOLEAN
                )
                """);
    }
}
