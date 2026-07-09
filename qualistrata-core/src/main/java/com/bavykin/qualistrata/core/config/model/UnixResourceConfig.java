package com.bavykin.qualistrata.core.config.model;

/**
 * Generic Unix host resource configuration.
 *
 * <p>The resource describes the physical/logical host and connection behavior.
 * Usernames, passwords, SSH keys, and other access data are resolved from the
 * separate secrets model through an access profile.</p>
 *
 * @param role optional consumer-defined logical role of the host.
 * @param host physical hostname or IP address.
 * @param port SSH or Unix adapter port.
 * @param defaultAccess default access profile alias.
 * @param commandTimeoutSeconds default command execution timeout in seconds.
 * @param shellProfile optional shell profile path or command that should be sourced before Unix command execution.
 * @param pool optional Unix connection pool configuration.
 */
public record UnixResourceConfig(
        String role,
        String host,
        Integer port,
        String defaultAccess,
        Integer commandTimeoutSeconds,
        String shellProfile,
        UnixPoolConfig pool
) {
}
