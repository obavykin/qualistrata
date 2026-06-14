package com.bavykin.qualistrata.core.config.model;

import java.util.Map;

/**
 * Describes one automation target environment.
 *
 * <p>A target environment is a named runtime configuration used by QualiStrata
 * to resolve UI, API, database, Unix, and performance-related settings.
 * This model is intentionally generic and must not contain product-specific
 * assumptions.</p>
 *
 * @param env logical target/environment name.
 * @param hosts configured Unix or infrastructure hosts by logical alias.
 * @param api API-related configuration.
 * @param db database-related configuration.
 * @param ui UI-related configuration.
 * @param perf performance and telemetry-related configuration.
 */
public record TargetConfig(
        String env,
        Map<String, HostConfig> hosts,
        APIConfig api,
        DBConfig db,
        UIConfig ui,
        PerfConfig perf
) {
}