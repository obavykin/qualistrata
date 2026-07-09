package com.bavykin.qualistrata.core.config.secrets;

/**
 * Unix/SSH access profile secret data.
 *
 * @param username login username.
 * @param password optional password.
 * @param keyPath optional private key path.
 * @param keyPassphrase optional private key passphrase.
 */
public record UnixAccessSecret(
        String username,
        String password,
        String keyPath,
        String keyPassphrase
) {
}
