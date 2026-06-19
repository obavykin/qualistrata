package com.bavykin.qualistrata.db.environment;

import com.bavykin.qualistrata.db.client.DbClient;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.provider.DbProvider;
import com.bavykin.qualistrata.db.provider.DbProviderRegistry;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Generic database connection environment check.
 *
 * <p>The check validates that QualiStrata can resolve a provider for the
 * configured database type, create a client, and execute the provider-specific
 * health-check SQL. It intentionally does not contain Oracle or PostgreSQL
 * branching; engine-specific behavior belongs to {@link DbProvider}.</p>
 */
public class DbConnectionEnvironmentCheck {

    private final DbProviderRegistry providerRegistry;

    /**
     * Creates a connection check backed by a provider registry.
     *
     * @param providerRegistry registry used to resolve the engine-specific provider.
     */
    public DbConnectionEnvironmentCheck(DbProviderRegistry providerRegistry) {
        if (providerRegistry == null) {
            throw new DbException("DB provider registry must not be null.");
        }
        this.providerRegistry = providerRegistry;
    }

    /**
     * Executes the connection check for the provided DB connection config.
     *
     * @param config resolved DB connection config.
     * @return check result containing safe diagnostics.
     */
    public DbEnvironmentCheckResult execute(DbConnectionConfig config) {
        Instant startedAt = Instant.now();

        try {
            DbProvider provider = providerRegistry.provider(config.type());
            String healthCheckSql = provider.defaultHealthCheckSql();

            try (DbClient client = provider.createClient(config)) {
                client.queryValue(healthCheckSql, Object.class);
            }

            return DbEnvironmentCheckResult.passed(
                    DbEnvironmentChecks.CONNECTION_CHECK_ID,
                    "Database connection check",
                    config.alias(),
                    config.type(),
                    "Database connection check passed.",
                    elapsedSince(startedAt),
                    Map.of("healthCheckSql", healthCheckSql)
            );
        } catch (Exception exception) {
            return DbEnvironmentCheckResult.failed(
                    DbEnvironmentChecks.CONNECTION_CHECK_ID,
                    "Database connection check",
                    safeAlias(config),
                    safeType(config),
                    SafeDbDiagnostics.failureMessage("Database connection check failed"),
                    elapsedSince(startedAt),
                    SafeDbDiagnostics.failureDetails(exception)
            );
        }
    }

    private static Duration elapsedSince(Instant startedAt) {
        return Duration.between(startedAt, Instant.now());
    }

    private static String safeAlias(DbConnectionConfig config) {
        return config == null ? null : config.alias();
    }

    private static com.bavykin.qualistrata.db.DbType safeType(DbConnectionConfig config) {
        return config == null ? null : config.type();
    }
}
