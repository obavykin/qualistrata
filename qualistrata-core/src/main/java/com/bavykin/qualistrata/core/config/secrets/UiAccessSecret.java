package com.bavykin.qualistrata.core.config.secrets;

/**
 * UI login access profile secret data.
 *
 * @param username application login username.
 * @param password application login password.
 * @param token optional token for UI authentication strategies that do not use
 *              username/password login.
 */
public record UiAccessSecret(
        String username,
        String password,
        String token
) {
}
