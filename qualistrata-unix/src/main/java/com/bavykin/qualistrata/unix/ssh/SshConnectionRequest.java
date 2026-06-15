package com.bavykin.qualistrata.unix.ssh;

import java.time.Duration;

/**
 * Request for opening a low-level SSH connection.
 *
 * <p>The request contains only transport-level connection information. It does
 * not contain target configuration objects directly so that SSH implementations
 * can stay independent from higher-level configuration models.</p>
 *
 * @param host remote SSH host address.
 * @param port remote SSH port.
 * @param username SSH username.
 * @param password optional SSH password.
 * @param keyPath optional private key path.
 * @param connectTimeout optional connection timeout.
 */
public record SshConnectionRequest(
        String host,
        Integer port,
        String username,
        String password,
        String keyPath,
        Duration connectTimeout
) {
}