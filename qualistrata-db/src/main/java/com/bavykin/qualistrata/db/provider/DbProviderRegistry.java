package com.bavykin.qualistrata.db.provider;

import com.bavykin.qualistrata.db.DbType;

import java.util.Collection;

/**
 * Registry of database providers available to the DB module.
 *
 * <p>Consumer projects must be able to add custom providers or override built-in
 * providers without modifying QualiStrata core code. This mirrors the planned
 * extensibility model for Unix component detectors.</p>
 */
public interface DbProviderRegistry {

    /**
     * Resolves a provider by database type.
     *
     * @param type database engine type.
     * @return matching provider.
     */
    DbProvider provider(DbType type);

    /**
     * Returns all currently registered providers.
     *
     * @return all registered providers.
     */
    Collection<DbProvider> providers();
}
