package com.bavykin.qualistrata.core.config.path;

import com.bavykin.qualistrata.core.exceptions.FrameworkException;

import java.nio.file.Path;

/**
 * Resolves conventional target configuration paths for a selected target.
 *
 * <p>This resolver keeps only generic repository-layout conventions. It does
 * not know about any specific product, customer, release name, or environment.
 * Consuming projects may use this convention directly or provide their own path
 * resolution logic on top of QualiStrata core primitives.</p>
 *
 * <p>The default convention is:</p>
 *
 * <pre>
 * config/targets/{target}.yml
 * config/secrets.yml
 * config/secrets/{target}.yml
 * explicit secrets file, when provided
 * </pre>
 *
 * <p>The resolver only builds paths. It does not check whether files exist,
 * does not load YAML, and does not merge configuration layers.</p>
 */
public class TargetConfigPathResolver {

    /**
     * Default directory containing public target configuration files.
     */
    public static final String TARGETS_DIRECTORY = "config/targets";

    /**
     * Default local secrets file path.
     */
    public static final String DEFAULT_SECRETS_FILE = "config/secrets.yml";

    /**
     * Default directory containing target-specific local secrets files.
     */
    public static final String TARGET_SECRETS_DIRECTORY = "config/secrets";

    /**
     * Creates a target configuration path resolver.
     *
     * <p>The resolver is stateless. A single instance can be reused across
     * multiple target resolution operations.</p>
     */
    public TargetConfigPathResolver() {
    }

    /**
     * Resolves conventional paths for the selected target.
     *
     * @param projectRoot root directory of the consuming project.
     * @param targetName selected target name.
     * @return resolved configuration layer paths.
     * @throws FrameworkException if project root or target name is missing.
     */
    public TargetConfigPaths resolve(Path projectRoot, String targetName) {
        return resolve(projectRoot, targetName, null);
    }

    /**
     * Resolves conventional paths for the selected target and an optional
     * explicit secrets file.
     *
     * <p>The explicit secrets path is resolved against the project root when it
     * is relative. Absolute explicit paths are preserved as-is.</p>
     *
     * @param projectRoot root directory of the consuming project.
     * @param targetName selected target name.
     * @param explicitSecretsPath optional explicit secrets file path.
     * @return resolved configuration layer paths.
     * @throws FrameworkException if project root or target name is missing.
     */
    public TargetConfigPaths resolve(
            Path projectRoot,
            String targetName,
            Path explicitSecretsPath
    ) {
        Path normalizedRoot = normalizeProjectRoot(projectRoot);
        String normalizedTarget = normalizeTargetName(targetName);

        return new TargetConfigPaths(
                normalizedTarget,
                normalizedRoot.resolve(TARGETS_DIRECTORY).resolve(normalizedTarget + ".yml"),
                normalizedRoot.resolve(DEFAULT_SECRETS_FILE),
                normalizedRoot.resolve(TARGET_SECRETS_DIRECTORY).resolve(normalizedTarget + ".yml"),
                resolveOptionalPath(normalizedRoot, explicitSecretsPath)
        );
    }

    /**
     * Normalizes and validates the project root path.
     *
     * @param projectRoot raw project root path.
     * @return normalized project root path.
     */
    private Path normalizeProjectRoot(Path projectRoot) {
        if (projectRoot == null) {
            throw new FrameworkException("Project root path must not be null.");
        }

        return projectRoot.toAbsolutePath().normalize();
    }

    /**
     * Normalizes and validates the target name.
     *
     * @param targetName raw target name.
     * @return trimmed target name.
     */
    private String normalizeTargetName(String targetName) {
        if (targetName == null) {
            throw new FrameworkException("Target name must not be null.");
        }

        String normalized = targetName.trim();

        if (normalized.isEmpty()) {
            throw new FrameworkException("Target name must not be blank.");
        }

        return normalized;
    }

    /**
     * Resolves an optional path against the project root when it is relative.
     *
     * @param projectRoot normalized project root path.
     * @param path optional raw path.
     * @return normalized absolute path, or {@code null} when the input is null.
     */
    private Path resolveOptionalPath(Path projectRoot, Path path) {
        if (path == null) {
            return null;
        }

        Path resolved = path.isAbsolute()
                ? path
                : projectRoot.resolve(path);

        return resolved.toAbsolutePath().normalize();
    }
}