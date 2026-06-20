package com.bavykin.qualistrata.core.config.resolved;

import com.bavykin.qualistrata.core.config.model.MetricsResourceConfig;
import com.bavykin.qualistrata.core.config.secrets.SecretValue;

/** Runtime metrics backend access configuration. */
public final class ResolvedMetricsConfig {
    private final String alias;
    private final String accessAlias;
    private final MetricsResourceConfig resource;
    private final String username;
    private final SecretValue password;
    private final SecretValue token;

    public ResolvedMetricsConfig(String alias, String accessAlias, MetricsResourceConfig resource,
                                 String username, SecretValue password, SecretValue token) {
        this.alias = alias;
        this.accessAlias = accessAlias;
        this.resource = resource;
        this.username = username;
        this.password = password;
        this.token = token;
    }

    public String alias() { return alias; }
    public String accessAlias() { return accessAlias; }
    public MetricsResourceConfig resource() { return resource; }
    public String type() { return resource == null ? null : resource.type(); }
    public Boolean enabled() { return resource == null ? null : resource.enabled(); }
    public String url() { return resource == null ? null : resource.url(); }
    public String org() { return resource == null ? null : resource.org(); }
    public String bucket() { return resource == null ? null : resource.bucket(); }
    public String username() { return username; }
    public SecretValue password() { return password; }
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
