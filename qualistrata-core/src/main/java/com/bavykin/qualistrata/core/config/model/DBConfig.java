package com.bavykin.qualistrata.core.config.model;

/**
 * Database connection configuration.
 *
 * <p>The configuration describes Oracle database endpoint used by
 * framework database utilities.
 *
 * <p>Both Oracle service name and SID are supported:
 *
 * <ul>
 *     <li>service name uses {@code jdbc:oracle:thin:@//host:port/service}</li>
 *     <li>SID uses {@code jdbc:oracle:thin:@host:port:sid}</li>
 * </ul>
 *
 * <p>If both {@code service} and {@code sid} are configured, service name
 * has priority.
 *
 * <p>Password is intentionally not part of this configuration and should be
 * provided through secure runtime configuration, for example system property
 * or CI credentials.
 *
 * @param host database host
 * @param port database listener port
 * @param service Oracle service name
 * @param sid Oracle SID
 * @param username database username
 */
public record DBConfig(

        String host,

        Integer port,

        String service,

        String sid,

        String username

) {
}