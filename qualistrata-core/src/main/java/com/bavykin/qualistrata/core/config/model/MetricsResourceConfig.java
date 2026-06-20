package com.bavykin.qualistrata.core.config.model;

/**
 * Generic metrics/telemetry backend resource configuration.
 *
 * <p>Metrics resources are optional. Functional test execution must not depend
 * on metrics backend availability unless a consumer project explicitly enables
 * strict behavior in its performance pipeline.</p>
 *
 * @param type metrics backend type, for example {@code influxdb}.
 * @param enabled whether the metrics backend is enabled for this target.
 * @param url backend URL.
 * @param org optional organization/tenant.
 * @param bucket optional bucket/database/project destination.
 * @param defaultAccess default access profile alias.
 */
public record MetricsResourceConfig(
        String type,
        Boolean enabled,
        String url,
        String org,
        String bucket,
        String defaultAccess
) {
}
