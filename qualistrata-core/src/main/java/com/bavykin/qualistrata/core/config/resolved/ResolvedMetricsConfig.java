package com.bavykin.qualistrata.core.config.resolved;

import com.bavykin.qualistrata.core.config.model.MetricsResourceConfig;
import com.bavykin.qualistrata.core.config.secrets.SecretValue;

/**
 * Runtime metrics backend access configuration.
 *
 * <p>Credentials are represented with {@link SecretValue} and are masked by
 * {@link #toString()} to prevent accidental leakage in diagnostics.</p>
 */
public final class ResolvedMetricsConfig {
    private final String alias;
    private final String accessAlias;
    private final MetricsResourceConfig resource;
    private final String username;
    private final SecretValue password;
    private final SecretValue token;

    /**
     * Creates resolved metrics backend access configuration.
     *
     * @param alias metrics resource alias.
     * @param accessAlias selected metrics access profile alias.
     * @param resource non-secret metrics backend resource configuration.
     * @param username resolved metrics username, or {@code null} when not configured.
     * @param password resolved metrics password, or {@code null} when not configured.
     * @param token resolved metrics token, or {@code null} when not configured.
     */
    public ResolvedMetricsConfig(String alias, String accessAlias, MetricsResourceConfig resource,
                                 String username, SecretValue password, SecretValue token) {
        this.alias = alias;
        this.accessAlias = accessAlias;
        this.resource = resource;
        this.username = username;
        this.password = password;
        this.token = token;
    }

    /**
     * Returns the resource alias.
     *
     * @return metrics resource alias.
     */
    public String alias() { return alias; }

    /**
     * Returns the selected access profile alias.
     *
     * @return selected metrics access profile alias.
     */
    public String accessAlias() { return accessAlias; }

    /**
     * Returns the original non-secret resource configuration.
     *
     * @return original non-secret metrics resource configuration.
     */
    public MetricsResourceConfig resource() { return resource; }

    /**
     * Returns the configured resource type.
     *
     * @return metrics backend type.
     */
    public String type() { return resource == null ? null : resource.type(); }

    /**
     * Returns whether the backend is enabled.
     *
     * @return whether the metrics backend is enabled.
     */
    public Boolean enabled() { return resource == null ? null : resource.enabled(); }

    /**
     * Returns the configured backend URL.
     *
     * @return metrics backend URL.
     */
    public String url() { return resource == null ? null : resource.url(); }

    /**
     * Returns the configured organization.
     *
     * @return metrics organization name.
     */
    public String org() { return resource == null ? null : resource.org(); }

    /**
     * Returns the configured bucket or database name.
     *
     * @return metrics bucket or database name.
     */
    public String bucket() { return resource == null ? null : resource.bucket(); }

    /**
     * Returns the resolved username.
     *
     * @return resolved metrics username, or {@code null} when not configured.
     */
    public String username() { return username; }

    /**
     * Returns the resolved password.
     *
     * @return resolved metrics password, or {@code null} when not configured.
     */
    public SecretValue password() { return password; }

    /**
     * Returns the resolved token.
     *
     * @return resolved metrics token, or {@code null} when not configured.
     */
    public SecretValue token() { return token; }

    @Override
    public String toString() {
        return "ResolvedMetricsConfig[alias='" + alias + '\'' +
                ", accessAlias='" + accessAlias + '\'' +
                ", type='" + type() + '\'' +
                ", enabled=" + enabled() +
                ", url='" + url() + '\'' +
                ", org='" + org() + '\'' +
                ", bucket='" + bucket() + '\'' +
                ", username='" + username + '\'' +
                ", password=" + (password == null ? null : password) +
                ", token=" + (token == null ? null : token) + ']';
    }
}
