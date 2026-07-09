package com.bavykin.qualistrata.core.config.resolution;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Request object for resolving the effective target configuration.
 *
 * <p>The request keeps runtime inputs explicit and testable. This avoids
 * reading global JVM state inside the resolver and allows consuming projects to
 * define their own conventions while reusing QualiStrata core primitives.</p>
 *
 * @param projectRoot root directory of the consuming project.
 * @param properties JVM system properties or test-provided equivalent.
 * @param environment operating system environment variables or test-provided
 *                    equivalent.
 * @param projectDefaultTarget optional target name supplied by the consuming
 *                             project as an explicit default.
 * @param explicitSecretsPath optional additional secrets file path.
 */
public record TargetConfigResolutionRequest(
        Path projectRoot,
        Properties properties,
        Map<String, String> environment,
        String projectDefaultTarget,
        Path explicitSecretsPath
) {
}