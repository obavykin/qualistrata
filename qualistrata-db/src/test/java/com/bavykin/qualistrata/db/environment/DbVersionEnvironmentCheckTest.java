package com.bavykin.qualistrata.db.environment;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.provider.DefaultDbProviderRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.bavykin.qualistrata.db.environment.DbEnvironmentCheckTestSupport.config;
import static com.bavykin.qualistrata.db.environment.DbEnvironmentCheckTestSupport.failure;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generic DB version environment check.
 */
class DbVersionEnvironmentCheckTest {

    @Test
    void shouldPassAndReportActualVersionWhenExpectedVersionIsNotProvided() {
        DbEnvironmentCheckTestSupport.FakeClient client =
                new DbEnvironmentCheckTestSupport.FakeClient("16.3");
        DbEnvironmentCheckTestSupport.FakeProvider provider =
                new DbEnvironmentCheckTestSupport.FakeProvider(
                        DbType.POSTGRES,
                        "SELECT 1",
                        "SHOW server_version",
                        client
                );

        DbVersionEnvironmentCheck check = new DbVersionEnvironmentCheck(
                new DefaultDbProviderRegistry(List.of(provider))
        );

        DbEnvironmentCheckResult result = check.execute(config());

        assertTrue(result.passed());
        assertEquals(DbEnvironmentCheckStatus.PASSED, result.status());
        assertEquals("db.version", result.id());
        assertEquals("16.3", result.details().get("actualVersion"));
        assertEquals("SHOW server_version", client.executedSql());
        assertTrue(client.closed());
    }

    @Test
    void shouldPassWhenActualVersionMatchesExpectedVersion() {
        DbEnvironmentCheckTestSupport.FakeClient client =
                new DbEnvironmentCheckTestSupport.FakeClient("16.3");
        DbEnvironmentCheckTestSupport.FakeProvider provider =
                new DbEnvironmentCheckTestSupport.FakeProvider(
                        DbType.POSTGRES,
                        "SELECT 1",
                        "SHOW server_version",
                        client
                );

        DbVersionEnvironmentCheck check = new DbVersionEnvironmentCheck(
                new DefaultDbProviderRegistry(List.of(provider))
        );

        DbEnvironmentCheckResult result = check.execute(config(), "16.3");

        assertTrue(result.passed());
        assertEquals("16.3", result.details().get("actualVersion"));
        assertEquals("16.3", result.details().get("expectedVersion"));
        assertTrue(client.closed());
    }

    @Test
    void shouldFailWhenActualVersionDoesNotMatchExpectedVersion() {
        DbEnvironmentCheckTestSupport.FakeClient client =
                new DbEnvironmentCheckTestSupport.FakeClient("16.2");
        DbEnvironmentCheckTestSupport.FakeProvider provider =
                new DbEnvironmentCheckTestSupport.FakeProvider(
                        DbType.POSTGRES,
                        "SELECT 1",
                        "SHOW server_version",
                        client
                );

        DbVersionEnvironmentCheck check = new DbVersionEnvironmentCheck(
                new DefaultDbProviderRegistry(List.of(provider))
        );

        DbEnvironmentCheckResult result = check.execute(config(), "16.3");

        assertFalse(result.passed());
        assertEquals(DbEnvironmentCheckStatus.FAILED, result.status());
        assertEquals("16.2", result.details().get("actualVersion"));
        assertEquals("16.3", result.details().get("expectedVersion"));
        assertTrue(client.closed());
    }

    @Test
    void shouldFailWhenVersionQueryFails() {
        DbEnvironmentCheckTestSupport.FakeClient client =
                new DbEnvironmentCheckTestSupport.FakeClient(failure("version query failed"));
        DbEnvironmentCheckTestSupport.FakeProvider provider =
                new DbEnvironmentCheckTestSupport.FakeProvider(
                        DbType.POSTGRES,
                        "SELECT 1",
                        "SHOW server_version",
                        client
                );

        DbVersionEnvironmentCheck check = new DbVersionEnvironmentCheck(
                new DefaultDbProviderRegistry(List.of(provider))
        );

        DbEnvironmentCheckResult result = check.execute(config(), "16.3");

        assertFalse(result.passed());
        assertEquals(DbEnvironmentCheckStatus.FAILED, result.status());
        assertEquals("Database version check failed.", result.message());
        assertTrue(result.details().containsKey("exceptionType"));
        assertTrue(client.closed());
    }

    @Test
    void shouldNotExposeRawExceptionMessageWhenVersionQueryFails() {
        String sensitiveMessage = "Version query failed for user tester with password=secret-password";
        DbEnvironmentCheckTestSupport.FakeClient client =
                new DbEnvironmentCheckTestSupport.FakeClient(failure(sensitiveMessage));
        DbEnvironmentCheckTestSupport.FakeProvider provider =
                new DbEnvironmentCheckTestSupport.FakeProvider(
                        DbType.POSTGRES,
                        "SELECT 1",
                        "SHOW server_version",
                        client
                );

        DbVersionEnvironmentCheck check = new DbVersionEnvironmentCheck(
                new DefaultDbProviderRegistry(List.of(provider))
        );

        DbEnvironmentCheckResult result = check.execute(config(), "16.3");

        assertFalse(result.passed());
        assertEquals("Database version check failed.", result.message());
        assertFalse(result.message().contains("secret-password"));
        assertFalse(result.details().toString().contains("secret-password"));
        assertEquals(DbException.class.getName(), result.details().get("exceptionType"));
    }
}
