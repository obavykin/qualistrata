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

    /**
     * Creates resolved Unix host access configuration.
     *
     * @param alias Unix resource alias.
     * @param accessAlias selected Unix access profile alias.
     * @param resource non-secret Unix resource topology.
     * @param username resolved Unix username.
     * @param password resolved Unix password, or {@code null} when key-based access is used.
     * @param keyPath path to private key, or {@code null} when password-based access is used.
     * @param keyPassphrase private key passphrase, or {@code null} when not configured.
     */
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

    /** @return Unix resource alias. */
    public String alias() { return alias; }

    /** @return selected Unix access profile alias. */
    public String accessAlias() { return accessAlias; }

    /** @return original non-secret Unix resource configuration. */
    public UnixResourceConfig resource() { return resource; }

    /** @return Unix resource role, for example {@code application} or {@code database}. */
    public String role() { return resource == null ? null : resource.role(); }

    /** @return Unix host address. */
    public String host() { return resource == null ? null : resource.host(); }

    /** @return SSH port. */
    public Integer port() { return resource == null ? null : resource.port(); }

    /** @return command timeout in seconds. */
    public Integer commandTimeoutSeconds() { return resource == null ? null : resource.commandTimeoutSeconds(); }

    /**
     * @return optional shell profile path or command for preparing the Unix shell environment
     * before consumer command execution.
     */
    public String shellProfile() { return resource == null ? null : resource.shellProfile(); }

    /** @return optional Unix session pool configuration. */
    public UnixPoolConfig pool() { return resource == null ? null : resource.pool(); }

    /** @return resolved Unix username. */
    public String username() { return username; }

    /** @return resolved Unix password, or {@code null} when not configured. */
    public SecretValue password() { return password; }

    /** @return private key path, or {@code null} when not configured. */
    public String keyPath() { return keyPath; }

    /** @return private key passphrase, or {@code null} when not configured. */
    public SecretValue keyPassphrase() { return keyPassphrase; }

    @Override
    public String toString() {
        return "ResolvedUnixHostConfig[" +
                "alias='" + alias + '\'' +
                ", accessAlias='" + accessAlias + '\'' +
                ", role='" + role() + '\'' +
                ", host='" + host() + '\'' +
                ", port=" + port() +
                ", shellProfile='" + shellProfile() + '\'' +
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
