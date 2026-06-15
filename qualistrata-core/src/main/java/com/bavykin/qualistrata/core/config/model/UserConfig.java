package com.bavykin.qualistrata.core.config.model;

/**
 * Resolved generic user configuration for a target host or service.
 *
 * <p>This record represents user configuration after open target settings and
 * optional secret values have been combined. Open values such as username or
 * key path may come from a committed target YAML file, while sensitive values
 * such as passwords must come from system properties, local secret files,
 * environment variables, CI/CD secret storage, or another dedicated secrets
 * mechanism.</p>
 *
 * <p>Real passwords must not be committed to the repository or stored in public
 * target configuration examples.</p>
 *
 * @param username login name used for the target user.
 * @param password optional resolved password value. The value must come from a
 *                 secrets mechanism and must not be stored in committed target
 *                 configuration files.
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