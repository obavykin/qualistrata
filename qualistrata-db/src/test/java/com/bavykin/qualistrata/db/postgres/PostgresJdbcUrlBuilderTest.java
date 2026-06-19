package com.bavykin.qualistrata.db.postgres;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PostgreSQL JDBC URL construction.
 *
 * <p>These tests do not require a PostgreSQL database or PostgreSQL JDBC driver.
 * They verify only provider-specific URL rules and validation behavior.</p>
 */
class PostgresJdbcUrlBuilderTest {

    @Test
    void shouldBuildUrlWithDefaultPort() {
        DbConnectionConfig config = postgresConfig(null);

        String jdbcUrl = PostgresJdbcUrlBuilder.build(config);

        assertEquals("jdbc:postgresql://pg-host:5432/reporting", jdbcUrl);
    }

    @Test
    void shouldBuildUrlWithExplicitPort() {
        DbConnectionConfig config = postgresConfig(15432);

        String jdbcUrl = PostgresJdbcUrlBuilder.build(config);

        assertEquals("jdbc:postgresql://pg-host:15432/reporting", jdbcUrl);
    }

    @Test
    void shouldRejectMissingHost() {
        DbConnectionConfig config = new DbConnectionConfig(
                "reporting",
                DbType.POSTGRES,
                " ",
                5432,
                "reporting",
                "public",
                "tester",
                "secret",
                null,
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Map.of()
        );

        assertThrows(DbException.class, () -> PostgresJdbcUrlBuilder.build(config));
    }

    @Test
    void shouldRejectMissingDatabase() {
        DbConnectionConfig config = new DbConnectionConfig(
                "reporting",
                DbType.POSTGRES,
                "pg-host",
                5432,
                " ",
                "public",
                "tester",
                "secret",
                null,
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Map.of()
        );

        assertThrows(DbException.class, () -> PostgresJdbcUrlBuilder.build(config));
    }

    @Test
    void shouldRejectWrongDbType() {
        DbConnectionConfig config = new DbConnectionConfig(
                "reporting",
                DbType.ORACLE,
                "pg-host",
                5432,
                "reporting",
                "public",
                "tester",
                "secret",
                null,
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Map.of()
        );

        assertThrows(DbException.class, () -> PostgresJdbcUrlBuilder.build(config));
    }

    private DbConnectionConfig postgresConfig(Integer port) {
        return new DbConnectionConfig(
                "reporting",
                DbType.POSTGRES,
                "pg-host",
                port,
                "reporting",
                "public",
                "tester",
                "secret",
                null,
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Map.of()
        );
    }
}
