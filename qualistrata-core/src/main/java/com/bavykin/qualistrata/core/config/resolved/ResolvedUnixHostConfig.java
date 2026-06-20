package com.bavykin.qualistrata.core.config.resolved;

import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UnixResourceConfig;
import com.bavykin.qualistrata.core.config.secrets.SecretValue;

/**
 * Runtime Unix host access configuration resolved from target resources and secrets.
 *
 * <p>This object is safe for diagnostics because sensitive values are masked in
 * {@link #toString()}. Client code must unwrap secrets only when passing them
 * to SSH or another Unix access adapter.</p>
 */
public final class ResolvedUnixHostConfig {

    private final String alias;
    private final String accessAlias;
    private final UnixResourceConfig resource;
    private final String username;
    private final SecretValue password;
    private final String keyPath;
    private final SecretValue keyPassphrase;

    public ResolvedUnixHostConfig(
            String alias,
            String accessAlias,
            UnixResourceConfig resource,
            String username,
            SecretValue password,
            String keyPath,
            SecretValue keyPassphrase
    ) {
        this.alias = alias;
        this.accessAlias = accessAlias;
        this.resource = resource;
        this.username = username;
        this.password = password;
        this.keyPath = keyPath;
        this.keyPassphrase = keyPassphrase;
    }

    public String alias() { return alias; }
    public String accessAlias() { return accessAlias; }
    public UnixResourceConfig resource() { return resource; }
    public String role() { return resource == null ? null : resource.role(); }
    public String host() { return resource == null ? null : resource.host(); }
    public Integer port() { return resource == null ? null : resource.port(); }
    public Integer commandTimeoutSeconds() { return resource == null ? null : resource.commandTimeoutSeconds(); }
    public UnixPoolConfig pool() { return resource == null ? null : resource.pool(); }
    public String username() { return username; }
    public SecretValue password() { return password; }
    public String keyPath() { return keyPath; }
    public SecretValue keyPassphrase() { return keyPassphrase; }

    @Override
    public String toString() {
        return "ResolvedUnixHostConfig[" +
                "alias='" + alias + '\'' +
                ", accessAlias='" + accessAlias + '\'' +
                ", role='" + role() + '\'' +
                ", host='" + host() + '\'' +
                ", port=" + port() +
                ", username='" + username + '\'' +
                ", password=" + mask(password) +
                ", keyPath='" + keyPath + '\'' +
                ", keyPassphrase=" + mask(keyPassphrase) +
                ']';
    }

    private String mask(SecretValue value) {
        return value == null ? null : value.toString();
    }
}
