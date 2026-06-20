package com.bavykin.qualistrata.core.config.resolved;

import com.bavykin.qualistrata.core.config.model.UiResourceConfig;
import com.bavykin.qualistrata.core.config.secrets.SecretValue;

/** Runtime UI access configuration resolved from target resources and secrets. */
public final class ResolvedUiConfig {
    private final String alias;
    private final String accessAlias;
    private final UiResourceConfig resource;
    private final String username;
    private final SecretValue password;
    private final SecretValue token;

    public ResolvedUiConfig(String alias, String accessAlias, UiResourceConfig resource,
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
    public UiResourceConfig resource() { return resource; }
    public String type() { return resource == null ? null : resource.type(); }
    public String baseUrl() { return resource == null ? null : resource.baseUrl(); }
    public String username() { return username; }
    public SecretValue password() { return password; }
    public SecretValue token() { return token; }

    @Override
    public String toString() {
        return "ResolvedUiConfig[alias='" + alias + '\'' +
                ", accessAlias='" + accessAlias + '\'' +
                ", type='" + type() + '\'' +
                ", baseUrl='" + baseUrl() + '\'' +
                ", username='" + username + '\'' +
                ", password=" + (password == null ? null : password) +
                ", token=" + (token == null ? null : token) + ']';
    }
}
