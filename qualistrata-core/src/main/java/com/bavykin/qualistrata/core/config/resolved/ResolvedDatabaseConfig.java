package com.bavykin.qualistrata.core.config.resolved;

import com.bavykin.qualistrata.core.config.model.DatabasePoolConfig;
import com.bavykin.qualistrata.core.config.model.DatabaseResourceConfig;
import com.bavykin.qualistrata.core.config.secrets.SecretValue;

/**
 * Runtime database access configuration resolved from target resources and secrets.
 */
public final class ResolvedDatabaseConfig {

    private final String alias;
    private final String accessAlias;
    private final DatabaseResourceConfig resource;
    private final String username;
    private final SecretValue password;
    private final SecretValue token;

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

    public String alias() { return alias; }
    public String accessAlias() { return accessAlias; }
    public DatabaseResourceConfig resource() { return resource; }
    public String type() { return resource == null ? null : resource.type(); }
    public String host() { return resource == null ? null : resource.host(); }
    public Integer port() { return resource == null ? null : resource.port(); }
    public String databaseName() { return resource == null ? null : resource.databaseName(); }
    public String schema() { return resource == null ? null : resource.schema(); }
    public String jdbcUrl() { return resource == null ? null : resource.jdbcUrl(); }
    public String connectionMode() { return resource == null ? null : resource.connectionMode(); }
    public Integer connectionTimeoutSeconds() { return resource == null ? null : resource.connectionTimeoutSeconds(); }
    public Integer queryTimeoutSeconds() { return resource == null ? null : resource.queryTimeoutSeconds(); }
    public DatabasePoolConfig pool() { return resource == null ? null : resource.pool(); }
    public String username() { return username; }
    public SecretValue password() { return password; }
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
