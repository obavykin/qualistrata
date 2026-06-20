package com.bavykin.qualistrata.core.config.model;

import com.bavykin.qualistrata.core.config.secrets.SecretsConfig;

/**
 * Represents the selected QualiStrata target with separately loaded secrets.
 *
 * <p>Secrets are intentionally not merged into {@link TargetConfig}. Runtime
 * clients should use an access resolver to combine resource topology with the
 * required access profile only when a concrete session/client is needed.</p>
 *
 * @param name selected target name.
 * @param config loaded target resource topology.
 * @param secrets loaded access secrets, or {@code null} when no secrets file is configured.
 */
public record ResolvedTarget(
        String name,
        TargetConfig config,
        SecretsConfig secrets
) {
}
