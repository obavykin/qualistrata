package com.bavykin.qualistrata.db.environment;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.provider.DefaultDbProviderRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.bavykin.qualistrata.db.environment.DbEnvironmentCheckTestSupport.config;
import static com.bavykin.qualistrata.db.environment.DbEnvironmentCheckTestSupport.failure;
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
        assertTrue(result.message().contains("Database connection check failed"));
        assertTrue(result.details().containsKey("exceptionType"));
        assertTrue(client.closed());
    }

    @Test
    void shouldRejectNullRegistry() {
        assertThrows(
                RuntimeException.class,
                () -> new DbConnectionEnvironmentCheck(null)
        );
    }
}
