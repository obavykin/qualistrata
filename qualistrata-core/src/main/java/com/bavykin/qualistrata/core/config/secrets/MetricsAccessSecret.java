package com.bavykin.qualistrata.core.config.secrets;

/**
 * Metrics backend access profile secret data.
 *
 * @param username optional username for metrics backends that require one.
 * @param password optional password for metrics backends that require one.
 * @param token optional write token, for example InfluxDB token.
 */
public record MetricsAccessSecret(
        String username,
        String password,
        String token
) {
}
