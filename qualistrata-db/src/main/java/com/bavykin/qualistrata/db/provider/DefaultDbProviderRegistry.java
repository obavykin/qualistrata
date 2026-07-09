package com.bavykin.qualistrata.db.provider;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.exception.DbException;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Default provider registry implementation.
 *
 * <p>The registry is intentionally constructed from a collection of providers.
 * This keeps the core flexible: built-in Oracle/PostgreSQL providers can be
 * passed in by production code later, while tests and consumer projects can use
 * custom providers or override a built-in provider for a specific engine.</p>
 */
public class DefaultDbProviderRegistry implements DbProviderRegistry {

    private final Map<DbType, DbProvider> providers;

    /**
     * Creates a registry from the supplied providers.
     *
     * @param providers providers to register. If several providers support the
     * same type, the later provider overrides the earlier one intentionally.
     */
    public DefaultDbProviderRegistry(Collection<DbProvider> providers) {
        this.providers = new EnumMap<>(DbType.class);
        if (providers != null) {
            providers.forEach(this::register);
        }
    }

    /**
     * Convenience factory for tests or for an initial configuration without any
     * provider implementations.
     *
     * @return empty registry.
     */
    public static DefaultDbProviderRegistry empty() {
        return new DefaultDbProviderRegistry(List.of());
    }

    /**
     * Registers or overrides a provider.
     *
     * @param provider provider to register.
     */
    public final void register(DbProvider provider) {
        if (provider == null) {
            throw new DbException("DB provider must not be null.");
        }
        if (provider.type() == null) {
            throw new DbException("DB provider type must not be null.");
        }
        providers.put(provider.type(), provider);
    }

    @Override
    public DbProvider provider(DbType type) {
        if (type == null) {
            throw new DbException("DB provider type must not be null.");
        }
        DbProvider provider = providers.get(type);
        if (provider == null) {
            throw new DbException(null, type, "No DB provider registered for type: " + type);
        }
        return provider;
    }

    @Override
    public Collection<DbProvider> providers() {
        return List.copyOf(providers.values());
    }
}
