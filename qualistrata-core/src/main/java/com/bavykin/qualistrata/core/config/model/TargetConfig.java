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
 * @param resources generic resource topology available for this target.
 */
public record TargetConfig(
        String env,
        ResourcesConfig resources
) {
}
