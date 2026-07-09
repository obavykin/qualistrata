package com.bavykin.qualistrata.db.config;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.exception.DbException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generic DB connection configuration model.
 *
 * <p>These tests are pure unit tests. They do not open real DB connections and
 * therefore belong to the standard unit-test source set.</p>
 */
class DbConnectionConfigTest {

    @Test
    void shouldCreateConfigWithDefaults() {
        DbConnectionConfig config = new DbConnectionConfig(
                "main",
                DbType.POSTGRES,
                "localhost",
                5432,
                "reporting",
                "public",
                "tester",
                "secret",
                null,
                null,
                null,
                null
        );

        assertEquals("main", config.alias());
        assertEquals(DbType.POSTGRES, config.type());
        assertEquals(Duration.ofSeconds(30), config.connectionTimeout());
        assertEquals(Duration.ofSeconds(30), config.queryTimeout());
        assertTrue(config.properties().isEmpty());
    }

    @Test
    void shouldDetectExplicitJdbcUrl() {
        DbConnectionConfig config = new DbConnectionConfig(
                "main",
                DbType.ORACLE,
                null,
                null,
                null,
                null,
                "tester",
                "secret",
                "jdbc:oracle:thin:@localhost:1521:XE",
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Map.of()
        );

        assertTrue(config.hasExplicitJdbcUrl());
    }

    @Test
    void shouldMaskPasswordAndJdbcUrlInToString() {
        DbConnectionConfig config = new DbConnectionConfig(
                "main",
                DbType.ORACLE,
                "localhost",
                1521,
                "XE",
                null,
                "tester",
                "super-secret-password",
                "jdbc:oracle:thin:tester/super-secret-password@localhost:1521:XE",
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Map.of("oracle.net.CONNECT_TIMEOUT", "10000")
        );

        String text = config.toString();

        assertFalse(text.contains("super-secret-password"));
        assertTrue(text.contains("password=<masked>"));
        assertTrue(text.contains("jdbcUrl=<configured>"));
    }

    @Test
    void shouldRejectBlankAlias() {
        assertThrows(
                DbException.class,
                () -> new DbConnectionConfig(
                        " ",
                        DbType.POSTGRES,
                        "localhost",
                        5432,
                        "reporting",
                        null,
                        "tester",
                        "secret",
                        null,
                        Duration.ofSeconds(10),
                        Duration.ofSeconds(20),
                        Map.of()
                )
        );
    }

    @Test
    void shouldRejectMissingType() {
        assertThrows(
                DbException.class,
                () -> new DbConnectionConfig(
                        "main",
                        null,
                        "localhost",
                        5432,
                        "reporting",
                        null,
                        "tester",
                        "secret",
                        null,
                        Duration.ofSeconds(10),
                        Duration.ofSeconds(20),
                        Map.of()
                )
        );
    }

    @Test
    void shouldRejectNonPositiveConnectionTimeout() {
        assertThrows(
                DbException.class,
                () -> new DbConnectionConfig(
                        "main",
                        DbType.POSTGRES,
                        "localhost",
                        5432,
                        "reporting",
                        null,
                        "tester",
                        "secret",
                        null,
                        Duration.ZERO,
                        Duration.ofSeconds(20),
                        Map.of()
                )
        );
    }

    @Test
    void shouldRejectNonPositiveQueryTimeout() {
        assertThrows(
                DbException.class,
                () -> new DbConnectionConfig(
                        "main",
                        DbType.POSTGRES,
                        "localhost",
                        5432,
                        "reporting",
                        null,
                        "tester",
                        "secret",
                        null,
                        Duration.ofSeconds(10),
                        Duration.ZERO,
                        Map.of()
                )
        );
    }
}