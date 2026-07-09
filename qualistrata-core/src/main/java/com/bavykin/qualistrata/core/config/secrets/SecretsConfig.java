package com.bavykin.qualistrata.core.config.secrets;

/**
 * Root model for QualiStrata secret configuration.
 *
 * <p>Secrets are intentionally loaded into a model separate from
 * {@code TargetConfig}. This prevents secret overlays from changing resource
 * topology objects and avoids coupling every resource model to every possible
 * credential field.</p>
 *
 * @param access access profile secrets grouped by generic resource category.
 */
public record SecretsConfig(
        AccessSecretsConfig access
) {
}
