package com.bavykin.qualistrata.core.config.resolved;

import com.bavykin.qualistrata.core.config.model.UiResourceConfig;
import com.bavykin.qualistrata.core.config.secrets.SecretValue;

/**
 * Runtime UI access configuration resolved from target resources and secrets.
 *
 * <p>This class is generic and does not know about consumer-project roles such
 * as admin, user, or forecaster. Those are represented only as access aliases.</p>
 */
public final class ResolvedUiConfig {
    private final String alias;
    private final String accessAlias;
    private final UiResourceConfig resource;
    private final String username;
    private final SecretValue password;
    private final SecretValue token;

    /**
     * Creates resolved UI access configuration.
     *
     * @param alias UI resource alias.
     * @param accessAlias selected UI access profile alias.
     * @param resource non-secret UI resource configuration.
     * @param username resolved UI username, or {@code null} when not configured.
     * @param password resolved UI password, or {@code null} when not configured.
     * @param token resolved UI token, or {@code null} when not configured.
     */
    public ResolvedUiConfig(String alias, String accessAlias, UiResourceConfig resource,
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
     * @return UI resource alias.
     */
    public String alias() { return alias; }

    /**
     * Returns the selected access profile alias.
     *
     * @return selected UI access profile alias.
     */
    public String accessAlias() { return accessAlias; }

    /**
     * Returns the original non-secret resource configuration.
     *
     * @return original non-secret UI resource configuration.
     */
    public UiResourceConfig resource() { return resource; }

    /**
     * Returns the configured resource type.
     *
     * @return UI resource type.
     */
    public String type() { return resource == null ? null : resource.type(); }

    /**
     * Returns the configured UI base URL.
     *
     * @return UI base URL.
     */
    public String baseUrl() { return resource == null ? null : resource.baseUrl(); }

    /**
     * Returns the resolved username.
     *
     * @return resolved UI username, or {@code null} when not configured.
     */
    public String username() { return username; }

    /**
     * Returns the resolved password.
     *
     * @return resolved UI password, or {@code null} when not configured.
     */
    public SecretValue password() { return password; }

    /**
     * Returns the resolved token.
     *
     * @return resolved UI token, or {@code null} when not configured.
     */
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
