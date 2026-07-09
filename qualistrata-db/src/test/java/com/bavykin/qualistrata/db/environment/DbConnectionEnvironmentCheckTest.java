package com.bavykin.qualistrata.db.environment;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.provider.DefaultDbProviderRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.bavykin.qualistrata.db.environment.DbEnvironmentCheckTestSupport.config;
import static com.bavykin.qualistrata.db.environment.DbEnvironmentCheckTestSupport.failure;
import static com.bavykin.qualistrata.db.environment.DbEnvironmentCheckTestSupport.sqlFailure;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generic DB connection environment check.
 */
class DbConnectionEnvironmentCheckTest {

    @Test
    void shouldPassWhenHealthQuerySucceeds() {
        DbEnvironmentCheckTestSupport.FakeClient client =
                new DbEnvironmentCheckTestSupport.FakeClient(1);
        DbEnvironmentCheckTestSupport.FakeProvider provider =
                new DbEnvironmentCheckTestSupport.FakeProvider(
                        DbType.POSTGRES,
                        "SELECT 1",
                        "SHOW server_version",
                        client
                );

        DbConnectionEnvironmentCheck check = new DbConnectionEnvironmentCheck(
                new DefaultDbProviderRegistry(List.of(provider))
        );

        DbEnvironmentCheckResult result = check.execute(config());

        assertTrue(result.passed());
        assertEquals(DbEnvironmentCheckStatus.PASSED, result.status());
        assertEquals("db.connection", result.id());
        assertEquals("main", result.dbAlias());
        assertEquals(DbType.POSTGRES, result.dbType());
        assertEquals("SELECT 1", result.details().get("healthCheckSql"));
        assertEquals("SELECT 1", client.executedSql());
        assertTrue(client.closed());
    }

    @Test
    void shouldFailWhenHealthQueryFails() {
        DbEnvironmentCheckTestSupport.FakeClient client =
                new DbEnvironmentCheckTestSupport.FakeClient(failure("connection refused"));
        DbEnvironmentCheckTestSupport.FakeProvider provider =
                new DbEnvironmentCheckTestSupport.FakeProvider(
                        DbType.POSTGRES,
                        "SELECT 1",
                        "SHOW server_version",
                        client
                );

        DbConnectionEnvironmentCheck check = new DbConnectionEnvironmentCheck(
                new DefaultDbProviderRegistry(List.of(provider))
        );

        DbEnvironmentCheckResult result = check.execute(config());

        assertFalse(result.passed());
        assertEquals(DbEnvironmentCheckStatus.FAILED, result.status());
        assertEquals("main", result.dbAlias());
        assertEquals(DbType.POSTGRES, result.dbType());
        assertEquals("Database connection check failed.", result.message());
        assertTrue(result.details().containsKey("exceptionType"));
        assertTrue(client.closed());
    }


    @Test
    void shouldReportSqlAndOracleDiagnosticsWhenHealthQueryFailsWithSqlException() {
        DbEnvironmentCheckTestSupport.FakeClient client =
                new DbEnvironmentCheckTestSupport.FakeClient(
                        sqlFailure("ORA-01017: invalid username/password; logon denied", "72000", 1017)
                );
        DbEnvironmentCheckTestSupport.FakeProvider provider =
                new DbEnvironmentCheckTestSupport.FakeProvider(
                        DbType.ORACLE,
                        "SELECT 1 FROM dual",
                        "SELECT banner FROM v$version",
                        client
                );

        DbConnectionEnvironmentCheck check = new DbConnectionEnvironmentCheck(
                new DefaultDbProviderRegistry(List.of(provider))
        );

        DbEnvironmentCheckResult result = check.execute(config(DbType.ORACLE));

        assertFalse(result.passed());
        assertEquals("72000", result.details().get("sqlState"));
        assertEquals("1017", result.details().get("sqlErrorCode"));
        assertEquals("ORA-01017", result.details().get("oraCode"));
        assertFalse(result.details().toString().contains("invalid username/password"));
    }

    @Test
    void shouldNotExposeRawExceptionMessageWhenHealthQueryFails() {
        String sensitiveMessage = "Connection failed for jdbc:postgresql://host:5432/db?password=secret-password";
        DbEnvironmentCheckTestSupport.FakeClient client =
                new DbEnvironmentCheckTestSupport.FakeClient(failure(sensitiveMessage));
        DbEnvironmentCheckTestSupport.FakeProvider provider =
                new DbEnvironmentCheckTestSupport.FakeProvider(
                        DbType.POSTGRES,
                        "SELECT 1",
                        "SHOW server_version",
                        client
                );

        DbConnectionEnvironmentCheck check = new DbConnectionEnvironmentCheck(
                new DefaultDbProviderRegistry(List.of(provider))
        );

        DbEnvironmentCheckResult result = check.execute(config());

        assertFalse(result.passed());
        assertEquals("Database connection check failed.", result.message());
        assertFalse(result.message().contains("secret-password"));
        assertFalse(result.message().contains("jdbc:postgresql"));
        assertFalse(result.details().toString().contains("secret-password"));
        assertFalse(result.details().toString().contains("jdbc:postgresql"));
        assertEquals(DbException.class.getName(), result.details().get("exceptionType"));
    }

    @Test
    void shouldRejectNullRegistry() {
        assertThrows(
                RuntimeException.class,
                () -> new DbConnectionEnvironmentCheck(null)
        );
    }
}
