package com.bavykin.qualistrata.core.config.secrets;

/**
 * Database access profile secret data.
 *
 * @param username database username.
 * @param password database password.
 * @param token optional token for engines or services that do not use passwords.
 */
public record DatabaseAccessSecret(
        String username,
        String password,
        String token
) {
}
