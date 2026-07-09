package com.bavykin.qualistrata.core.config.model;

/**
 * Describes one QualiStrata automation target environment.
 *
 * <p>The target configuration contains only non-secret resource topology and
 * generic resource settings. It deliberately does not contain credentials,
 * passwords, tokens, private key passphrases, or other secret material.</p>
 *
 * <p>Consumer projects must describe their project-specific aliases in their
 * own integration layer. QualiStrata core only understands generic resource
 * categories such as UI applications, Unix hosts, databases, APIs, and metrics
 * backends.</p>
 *
 * @param env logical target/environment name.
 * @param release optional release or baseline identifier shared by several
 *                executable target environments.
 * @param resources generic resource topology available for this target.
 */
public record TargetConfig(
        String env,
        String release,
        ResourcesConfig resources
) {

    /**
     * Creates a target configuration without an explicit release identifier.
     *
     * <p>This constructor preserves source compatibility for existing tests and
     * consumers that only distinguish target environments by {@code env} and
     * resource topology.</p>
     *
     * @param env logical target/environment name.
     * @param resources generic resource topology available for this target.
     */
    public TargetConfig(
            String env,
            ResourcesConfig resources
    ) {
        this(env, null, resources);
    }
}
