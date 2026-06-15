package com.bavykin.qualistrata.core.config.path;

import java.nio.file.Path;

/**
 * Describes conventional file-system paths used during target configuration resolution.
 *
 * <p>The record does not load or validate any file. It only represents where
 * configuration layers are expected to be located for a selected target.</p>
 *
 * <p>All paths are optional from the perspective of this model. The final
 * resolution service decides which files are mandatory and which files may be
 * absent.</p>
 *
 * @param targetName selected target name.
 * @param targetConfigPath path to the public target configuration file.
 * @param defaultSecretsPath path to the optional default local secrets file.
 * @param targetSecretsPath path to the optional target-specific local secrets file.
 * @param explicitSecretsPath optional explicitly provided secrets file path.
 */
public record TargetConfigPaths(
        String targetName,
        Path targetConfigPath,
        Path defaultSecretsPath,
        Path targetSecretsPath,
        Path explicitSecretsPath
) {
}