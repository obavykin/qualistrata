package com.bavykin.qualistrata.db.provider;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.client.DbClient;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for provider registry behavior.
 *
 * <p>The registry must allow provider override because consumer projects may need
 * to replace a built-in provider with a project-specific implementation without
 * changing QualiStrata source code.</p>
 */
class DefaultDbProviderRegistryTest {

    @Test
    void shouldReturnRegisteredProvider() {
        DbProvider postgres = new FakeProvider(DbType.POSTGRES, "SELECT 1", "SHOW server_version");
        DefaultDbProviderRegistry registry = new DefaultDbProviderRegistry(List.of(postgres));

        assertSame(postgres, registry.provider(DbType.POSTGRES));
        assertEquals(1, registry.providers().size());
    }

    @Test
    void shouldAllowProviderOverrideByType() {
        DbProvider original = new FakeProvider(DbType.ORACLE, "SELECT 1 FROM dual", "original");
        DbProvider override = new FakeProvider(DbType.ORACLE, "SELECT 1 FROM dual", "override");

        DefaultDbProviderRegistry registry = new DefaultDbProviderRegistry(List.of(original, override));

        assertSame(override, registry.provider(DbType.ORACLE));
        assertEquals(1, registry.providers().size());
    }

    @Test
    void shouldFailWhenProviderIsMissing() {
        DefaultDbProviderRegistry registry = DefaultDbProviderRegistry.empty();

        assertThrows(
                DbException.class,
                () -> registry.provider(DbType.ORACLE)
        );
    }

    @Test
    void shouldRejectNullProvider() {
        DefaultDbProviderRegistry registry = DefaultDbProviderRegistry.empty();

        assertThrows(
                DbException.class,
                () -> registry.register(null)
        );
    }

    @Test
    void shouldRejectProviderWithNullType() {
        DbProvider invalidProvider = new FakeProvider(null, "SELECT 1", "SELECT version()");

        assertThrows(
                DbException.class,
                () -> new DefaultDbProviderRegistry(List.of(invalidProvider))
        );
    }

    private record FakeProvider(
            DbType type,
            String defaultHealthCheckSql,
            String databaseVersionSql
    ) implements DbProvider {

        @Override
        public DbClient createClient(DbConnectionConfig config) {
            throw new UnsupportedOperationException("Fake provider does not create real clients.");
        }
    }
}