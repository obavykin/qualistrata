package com.bavykin.qualistrata.db.environment;

import com.bavykin.qualistrata.db.client.DbClient;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.provider.DbProvider;
import com.bavykin.qualistrata.db.provider.DbProviderRegistry;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic database server version environment check.
 *
 * <p>The check asks the resolved {@link DbProvider} for engine-specific version
 * SQL, executes it through the generic {@link DbClient}, and optionally compares
 * the returned version with an expected baseline value. More advanced matchers
 * can be added later without changing provider responsibilities.</p>
 */
public class DbVersionEnvironmentCheck {

    private final DbProviderRegistry providerRegistry;

    /**
     * Creates a version check backed by a provider registry.
     *
     * @param providerRegistry registry used to resolve the engine-specific provider.
     */
    public DbVersionEnvironmentCheck(DbProviderRegistry providerRegistry) {
        if (providerRegistry == null) {
            throw new DbException("DB provider registry must not be null.");
        }
        this.providerRegistry = providerRegistry;
    }

    /**
     * Executes a version check without comparing the actual version to a baseline.
     *
     * @param config resolved DB connection config.
     * @return check result containing the actual version.
     */
    public DbEnvironmentCheckResult execute(DbConnectionConfig config) {
        return execute(config, null);
    }

    /**
     * Executes a version check and optionally compares the result to an expected
     * exact version value.
     *
     * @param config resolved DB connection config.
     * @param expectedVersion expected exact version, or {@code null} to only read
     *                        and report the actual version.
     * @return check result containing safe diagnostics.
     */
    public DbEnvironmentCheckResult execute(DbConnectionConfig config, String expectedVersion) {
        Instant startedAt = Instant.now();

        try {
            DbProvider provider = providerRegistry.provider(config.type());
            String versionSql = provider.databaseVersionSql();
            String actualVersion;

            try (DbClient client = provider.createClient(config)) {
                actualVersion = client.queryValue(versionSql, String.class);
            }

            Map<String, String> details = new LinkedHashMap<>();
            details.put("versionSql", versionSql);
            details.put("actualVersion", actualVersion);
            if (expectedVersion != null) {
                details.put("expectedVersion", expectedVersion);
            }

            if (expectedVersion != null && !expectedVersion.equals(actualVersion)) {
                return DbEnvironmentCheckResult.failed(
                        DbEnvironmentChecks.VERSION_CHECK_ID,
                        "Database version check",
                        config.alias(),
                        config.type(),
                        "Database version does not match expected baseline.",
                        elapsedSince(startedAt),
                        details
                );
            }

            return DbEnvironmentCheckResult.passed(
                    DbEnvironmentChecks.VERSION_CHECK_ID,
                    "Database version check",
                    config.alias(),
                    config.type(),
                    "Database version check passed.",
                    elapsedSince(startedAt),
                    details
            );
        } catch (Exception exception) {
            return DbEnvironmentCheckResult.failed(
                    DbEnvironmentChecks.VERSION_CHECK_ID,
                    "Database version check",
                    safeAlias(config),
                    safeType(config),
                    SafeDbDiagnostics.failureMessage("Database version check failed"),
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
