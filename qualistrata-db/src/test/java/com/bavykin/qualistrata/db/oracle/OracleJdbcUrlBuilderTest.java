package com.bavykin.qualistrata.db.oracle;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Oracle JDBC URL construction.
 *
 * <p>These tests do not require an Oracle database or Oracle JDBC driver. They
 * verify only provider-specific URL rules and validation behavior.</p>
 */
class OracleJdbcUrlBuilderTest {

    @Test
    void shouldBuildServiceUrlByDefault() {
        DbConnectionConfig config = oracleConfig(null, Map.of());

        String jdbcUrl = OracleJdbcUrlBuilder.build(config);

        assertEquals("jdbc:oracle:thin:@//db-host:1521/TNT65", jdbcUrl);
    }

    @Test
    void shouldBuildSidUrlWhenSidModeConfigured() {
        DbConnectionConfig config = oracleConfig(
                1522,
                Map.of(OracleJdbcUrlBuilder.CONNECTION_MODE_PROPERTY, OracleJdbcUrlBuilder.SID_MODE)
        );

        String jdbcUrl = OracleJdbcUrlBuilder.build(config);

        assertEquals("jdbc:oracle:thin:@db-host:1522:TNT65", jdbcUrl);
    }

    @Test
    void shouldRejectUnsupportedConnectionMode() {
        DbConnectionConfig config = oracleConfig(
                1521,
                Map.of(OracleJdbcUrlBuilder.CONNECTION_MODE_PROPERTY, "unknown")
        );

        assertThrows(DbException.class, () -> OracleJdbcUrlBuilder.build(config));
    }

    @Test
    void shouldRejectMissingHost() {
        DbConnectionConfig config = new DbConnectionConfig(
                "main",
                DbType.ORACLE,
                " ",
                1521,
                "TNT65",
                null,
                "tester",
                "secret",
                null,
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Map.of()
        );

        assertThrows(DbException.class, () -> OracleJdbcUrlBuilder.build(config));
    }

    @Test
    void shouldRejectMissingDatabase() {
        DbConnectionConfig config = new DbConnectionConfig(
                "main",
                DbType.ORACLE,
                "db-host",
                1521,
                " ",
                null,
                "tester",
                "secret",
                null,
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Map.of()
        );

        assertThrows(DbException.class, () -> OracleJdbcUrlBuilder.build(config));
    }

    @Test
    void shouldRejectWrongDbType() {
        DbConnectionConfig config = new DbConnectionConfig(
                "main",
                DbType.POSTGRES,
                "db-host",
                1521,
                "TNT65",
                null,
                "tester",
                "secret",
                null,
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Map.of()
        );

        assertThrows(DbException.class, () -> OracleJdbcUrlBuilder.build(config));
    }

    private DbConnectionConfig oracleConfig(Integer port, Map<String, String> properties) {
        return new DbConnectionConfig(
                "main",
                DbType.ORACLE,
                "db-host",
                port,
                "TNT65",
                null,
                "tester",
                "secret",
                null,
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                properties
        );
    }
}
