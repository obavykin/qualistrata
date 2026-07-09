package com.bavykin.qualistrata.core.config.model;

import java.util.Map;

/**
 * Configuration of a logical host in a target environment.
 *
 * <p>A logical host is addressed by alias from {@code TargetConfig.hosts()},
 * for example {@code app}, {@code db}, {@code mq}, or {@code reporting}.
 *
 * <p>The configuration describes how the framework connects to and interacts
 * with the host, including network access, user accounts, connection pooling,
 * execution settings, and host-specific integration properties.
 *
 * @param role logical role of the host
 * @param host physical hostname or IP address
 * @param port SSH port
 * @param defaultUser default Unix user alias used by the framework when no explicit user is requested
 * @param users user accounts configured for the host
 * @param oracleService Oracle service name or SID used by database integrations
 * @param oracleUser Oracle database username used by SQL*Plus-based integrations
 * @param pool SSH connection pool configuration
 * @param commandTimeoutSeconds default command execution timeout in seconds
 */
public record HostConfig(
        String role,
        String host,
        Integer port,
        String defaultUser,
        Map<String, UserConfig> users,
        String oracleUser,
        String oracleService,
        UnixPoolConfig pool,
        Integer commandTimeoutSeconds
) {
}