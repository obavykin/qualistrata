/**
 * Database environment checks implemented on top of the generic QualiStrata DB API.
 *
 * <p>This package contains reusable checks such as connection and database version
 * validation. The checks depend on {@code DbProviderRegistry} and {@code DbClient}
 * abstractions instead of database-specific implementation details, so they remain
 * engine-neutral and can work with Oracle, PostgreSQL, or future providers.</p>
 */
package com.bavykin.qualistrata.db.environment;
