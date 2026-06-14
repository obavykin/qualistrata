package com.bavykin.qualistrata.core.config.model;

/**
 * Generic user configuration for a target host or service.
 *
 * <p>This record describes a logical user account that can be referenced by
 * framework modules or project-specific integrations. It supports both
 * password-based and key-based authentication because different environments
 * may use different access models.</p>
 *
 * <p>Configuration files committed to source control must not contain real
 * passwords, private keys, tokens, or other sensitive values. Real secrets
 * should be provided through local secret files, CI/CD secret storage,
 * environment variables, or another dedicated secrets mechanism.</p>
 *
 * @param username login name used for the target user.
 * @param password optional password value or placeholder resolved from a
 *                 secrets mechanism. Real passwords must not be committed to
 *                 the repository.
 * @param keyPath optional path to a private key file used for key-based
 *                authentication. The key file itself must remain outside the
 *                repository.
 */
public record UserConfig(
        String username,
        String password,
        String keyPath
) {
}