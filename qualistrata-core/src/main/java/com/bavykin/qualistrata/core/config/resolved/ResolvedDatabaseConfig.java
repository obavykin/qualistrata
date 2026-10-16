package com.bavykin.qualistrata.core.config.resolved;

import com.bavykin.qualistrata.core.config.model.DatabasePoolConfig;
import com.bavykin.qualistrata.core.config.model.DatabaseResourceConfig;
import com.bavykin.qualistrata.core.config.secrets.SecretValue;

/**
 * Runtime database access configuration resolved from target resources and secrets.
 *
 * <p>The object may carry sensitive values through {@link SecretValue}. Its
 * {@link #toString()} implementation masks those values so it is safe for
 * diagnostics and reports that include resolved configuration metadata.</p>
 */
public final class ResolvedDatabaseConfig {

    private final String alias;
    private final String accessAlias;
    private final DatabaseResourceConfig resource;
    private final String username;
    private final SecretValue password;
    private final SecretValue token;

    /**
     * Creates resolved database access configuration.
     *
     * @param alias database resource alias.
     * @param accessAlias selected database access profile alias.
     * @param resource non-secret database resource topology.
     * @param username resolved database username.
     * @param password resolved database password, or {@code null} when not configured.
     * @param token resolved database token, or {@code null} when not configured.
     */
    public ResolvedDatabaseConfig(
            String alias,
            String accessAlias,
            DatabaseResourceConfig resource,
            String username,
            SecretValue password,
            SecretValue token
    ) {
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
     * @return database resource alias.
     */
    public String alias() { return alias; }

    /**
     * Returns the selected access profile alias.
     *
     * @return selected database access profile alias.
     */
    public String accessAlias() { return accessAlias; }

    /**
     * Returns the original non-secret resource configuration.
     *
     * @return original non-secret database resource configuration.
     */
    public DatabaseResourceConfig resource() { return resource; }

    /**
     * Returns the configured resource type.
     *
     * @return database engine type, for example {@code oracle} or {@code postgresql}.
     */
    public String type() { return resource == null ? null : resource.type(); }

    /**
     * Returns the configured host.
     *
     * @return database host when a host/port connection model is used.
     */
    public String host() { return resource == null ? null : resource.host(); }

    /**
     * Returns the configured port.
     *
     * @return database listener port when a host/port connection model is used.
     */
    public Integer port() { return resource == null ? null : resource.port(); }

    /**
     * Returns the databaseName.
     *
     * @return logical database name, SID, or service name depending on provider mapping.
     */
    public String databaseName() { return resource == null ? null : resource.databaseName(); }

    /**
     * Returns the configured default schema.
     *
     * @return optional default schema.
     */
    public String schema() { return resource == null ? null : resource.schema(); }

    /**
     * Returns the configured JDBC URL.
     *
     * @return optional JDBC URL without credentials.
     */
    public String jdbcUrl() { return resource == null ? null : resource.jdbcUrl(); }

    /**
     * Returns the provider-specific connection mode.
     *
     * @return provider-specific connection mode, for example {@code sid} or {@code service}.
     */
    public String connectionMode() { return resource == null ? null : resource.connectionMode(); }

    /**
     * Returns the connectionTimeoutSeconds.
     *
     * @return connection timeout in seconds.
     */
    public Integer connectionTimeoutSeconds() { return resource == null ? null : resource.connectionTimeoutSeconds(); }

    /**
     * Returns the query timeout.
     *
     * @return query timeout in seconds.
     */
    public Integer queryTimeoutSeconds() { return resource == null ? null : resource.queryTimeoutSeconds(); }

    /**
     * Returns the optional pool configuration.
     *
     * @return optional database pool configuration.
     */
    public DatabasePoolConfig pool() { return resource == null ? null : resource.pool(); }

    /**
     * Returns the resolved username.
     *
     * @return resolved database username.
     */
    public String username() { return username; }

    /**
     * Returns the resolved password.
     *
     * @return resolved database password, or {@code null} when not configured.
     */
    public SecretValue password() { return password; }

    /**
     * Returns the resolved token.
     *
     * @return resolved database token, or {@code null} when not configured.
     */
    public SecretValue token() { return token; }

    @Override
    public String toString() {
        return "ResolvedDatabaseConfig[" +
                "alias='" + alias + '\'' +
                ", accessAlias='" + accessAlias + '\'' +
                ", type='" + type() + '\'' +
                ", host='" + host() + '\'' +
                ", port=" + port() +
                ", databaseName='" + databaseName() + '\'' +
                ", schema='" + schema() + '\'' +
                ", jdbcUrl=" + maskConfigured(jdbcUrl()) +
                ", connectionMode='" + connectionMode() + '\'' +
                ", username='" + username + '\'' +
                ", password=" + mask(password) +
                ", token=" + mask(token) +
                ']';
    }

    private String mask(SecretValue value) { return value == null ? null : value.toString(); }
    private String maskConfigured(String value) { return value == null || value.isBlank() ? null : "<configured>"; }
}
