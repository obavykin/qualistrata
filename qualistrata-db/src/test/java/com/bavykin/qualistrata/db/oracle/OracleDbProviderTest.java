package com.bavykin.qualistrata.db.oracle;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Oracle provider metadata and validation.
 *
 * <p>The tests intentionally avoid opening a real Oracle connection. Real
 * connectivity belongs to guarded integration tests added later.</p>
 */
class OracleDbProviderTest {

    @Test
    void shouldExposeOracleType() {
        OracleDbProvider provider = new OracleDbProvider();

        assertEquals(DbType.ORACLE, provider.type());
    }

    @Test
    void shouldExposeOracleHealthCheckSql() {
        OracleDbProvider provider = new OracleDbProvider();

        assertEquals("SELECT 1 FROM dual", provider.defaultHealthCheckSql());
    }

    @Test
    void shouldExposeOracleVersionSql() {
        OracleDbProvider provider = new OracleDbProvider();

        assertTrue(provider.databaseVersionSql().contains("version_full"));
        assertTrue(provider.databaseVersionSql().contains("product_component_version"));
        assertTrue(provider.databaseVersionSql().contains("Oracle Database"));
    }

    @Test
    void shouldRejectWrongConfigTypeBeforeOpeningConnection() {
        OracleDbProvider provider = new OracleDbProvider();
        DbConnectionConfig postgresConfig = new DbConnectionConfig(
                "main",
                DbType.POSTGRES,
                "db-host",
                5432,
                "reporting",
                null,
                "tester",
                "secret",
                "jdbc:postgresql://db-host:5432/reporting",
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Map.of()
        );

        assertThrows(DbException.class, () -> provider.createClient(postgresConfig));
    }
}
