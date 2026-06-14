package com.bavykin.qualistrata.core.config.model;

/**
 * Represents the fully resolved target selected for the current framework run.
 *
 * <p>The resolved target combines the target name with its loaded configuration.
 * It is used by framework modules and project-specific integrations to access
 * environment settings without re-reading configuration files.</p>
 *
 * @param name selected target name.
 * @param config loaded target configuration.
 */
public record ResolvedTarget(
        String name,
        TargetConfig config
) {
}
