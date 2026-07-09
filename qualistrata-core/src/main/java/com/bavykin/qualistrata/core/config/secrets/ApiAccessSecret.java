package com.bavykin.qualistrata.core.config.secrets;

/**
 * API access profile secret data.
 *
 * @param username optional username for basic auth or client credentials.
 * @param password optional password for basic auth or client credentials.
 * @param token optional bearer/API token.
 * @param clientId optional OAuth/client credential id.
 * @param clientSecret optional OAuth/client credential secret.
 */
public record ApiAccessSecret(
        String username,
        String password,
        String token,
        String clientId,
        String clientSecret
) {
}
