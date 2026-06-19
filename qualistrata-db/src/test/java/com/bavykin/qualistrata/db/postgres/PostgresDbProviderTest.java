package com.bavykin.qualistrata.db.postgres;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PostgreSQL provider metadata and validation.
 *
 * <p>The tests intentionally avoid opening a real PostgreSQL connection. Real
 * connectivity belongs to guarded integration tests added later.</p>
 */
class PostgresDbProviderTest {

    @Test
    void shouldExposePostgresType() {
        PostgresDbProvider provider = new PostgresDbProvider();

        assertEquals(DbType.POSTGRES, provider.type());
    }

    @Test
    void shouldExposePostgresHealthCheckSql() {
        PostgresDbProvider provider = new PostgresDbProvider();

        assertEquals("SELECT 1", provider.defaultHealthCheckSql());
    }

    @Test
    void shouldExposePostgresVersionSql() {
        PostgresDbProvider provider = new PostgresDbProvider();

        assertEquals("SHOW server_version", provider.databaseVersionSql());
    }

    @Test
    void shouldRejectWrongConfigTypeBeforeOpeningConnection() {
        PostgresDbProvider provider = new PostgresDbProvider();
        DbConnectionConfig oracleConfig = new DbConnectionConfig(
                "main",
                DbType.ORACLE,
                "db-host",
                1521,
                "TNT65",
                null,
                "tester",
                "secret",
                "jdbc:oracle:thin:@//db-host:1521/TNT65",
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Map.of()
        );

        assertThrows(DbException.class, () -> provider.createClient(oracleConfig));
    }
}
