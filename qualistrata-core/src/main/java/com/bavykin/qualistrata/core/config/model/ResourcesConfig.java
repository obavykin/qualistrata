package com.bavykin.qualistrata.core.config.model;

import java.util.Map;

/**
 * Generic resource topology configured for one target environment.
 *
 * <p>Each map is keyed by a consumer-defined logical resource alias. Aliases
 * are intentionally project-specific data. The core framework stores and
 * resolves them but must not hardcode aliases such as {@code app},
 * {@code main}, {@code admin}, or {@code tnt-oracle}.</p>
 *
 * @param ui web UI applications or browser-accessible frontends.
 * @param unix Unix-like hosts reachable through SSH or other Unix adapters.
 * @param databases database endpoints reachable through DB client modules.
 * @param api API endpoints for future reusable API support.
 * @param metrics metrics/telemetry backends for optional observability support.
 */
public record ResourcesConfig(
        Map<String, UiResourceConfig> ui,
        Map<String, UnixResourceConfig> unix,
        Map<String, DatabaseResourceConfig> databases,
        Map<String, ApiResourceConfig> api,
        Map<String, MetricsResourceConfig> metrics
) {
}
