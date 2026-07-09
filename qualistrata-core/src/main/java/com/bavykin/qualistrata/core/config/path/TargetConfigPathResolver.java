package com.bavykin.qualistrata.core.config.path;

import com.bavykin.qualistrata.core.exceptions.FrameworkException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves generic target configuration paths for a selected target.
 *
 * <p>This resolver keeps only generic repository-layout conventions. It does
 * not know about any specific product, customer, release name, or environment.
 * Consuming projects may use this convention directly or provide their own path
 * resolution logic on top of QualiStrata core primitives.</p>
 *
 * <p>The preferred target configuration convention is:</p>
 *
 * <pre>
 * config/releases/{target}/target.yml
 * </pre>
 *
 * <p>The legacy target configuration convention is still supported for backward
 * compatibility:</p>
 *
 * <pre>
 * config/targets/{target}.yml
 * </pre>
 *
 * <p>An explicit target configuration file can be provided with:</p>
 *
 * <pre>
 * -Dtarget.config.file=/path/to/target.yml
 * </pre>
 *
 * <p>Secrets path conventions remain compatible with earlier QualiStrata
 * versions:</p>
 *
 * <pre>
 * config/secrets.yml
 * config/secrets/{target}.yml
 * -Dtarget.secrets.file=/path/to/secrets.yml
 * -Dsecrets.file=/path/to/secrets.yml
 * </pre>
 *
 * <p>The resolver validates target configuration file existence because it must
 * choose between the preferred convention, the legacy convention, and explicit
 * override. Secrets files are still returned as candidate paths and are not
 * required to exist at this stage.</p>
 */
public class TargetConfigPathResolver {

    /**
     * System property for explicit target configuration file override.
     */
    public static final String TARGET_CONFIG_FILE_PROPERTY = "target.config.file";

    /**
     * Preferred system property for explicit secrets file override.
     */
    public static final String TARGET_SECRETS_FILE_PROPERTY = "target.secrets.file";

    /**
     * Legacy system property for explicit secrets file override.
     */
    public static final String LEGACY_SECRETS_FILE_PROPERTY = "secrets.file";

    /**
     * Preferred directory containing release/target-scoped configuration
     * folders.
     */
    public static final String RELEASES_DIRECTORY = "config/releases";

    /**
     * Conventional target configuration file name inside a release/target
     * folder.
     */
    public static final String TARGET_CONFIG_FILE_NAME = "target.yml";

    /**
     * Legacy directory containing public target configuration files.
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
     * Resolves configuration paths for the selected target using system
     * property overrides when they are present.
     *
     * @param projectRoot root directory of the consuming project.
     * @param targetName selected target name.
     * @return resolved configuration layer paths.
     * @throws FrameworkException if project root, target name, or target config
     *                            file resolution is invalid.
     */
    public TargetConfigPaths resolve(Path projectRoot, String targetName) {
        return resolve(
                projectRoot,
                targetName,
                explicitPathFromSystemProperty(TARGET_CONFIG_FILE_PROPERTY),
                explicitSecretsPathFromSystemProperties()
        );
    }

    /**
     * Resolves configuration paths for the selected target using an optional
     * explicit secrets file.
     *
     * <p>This overload is kept for backward compatibility with existing
     * consumers that pass explicit secrets path programmatically.</p>
     *
     * @param projectRoot root directory of the consuming project.
     * @param targetName selected target name.
     * @param explicitSecretsPath optional explicit secrets file path.
     * @return resolved configuration layer paths.
     * @throws FrameworkException if project root, target name, or target config
     *                            file resolution is invalid.
     */
    public TargetConfigPaths resolve(
            Path projectRoot,
            String targetName,
            Path explicitSecretsPath
    ) {
        return resolve(
                projectRoot,
                targetName,
                explicitPathFromSystemProperty(TARGET_CONFIG_FILE_PROPERTY),
                explicitSecretsPath
        );
    }

    /**
     * Resolves configuration paths for the selected target using optional
     * explicit target configuration and secrets files.
     *
     * <p>The explicit target configuration path has the highest priority. When
     * it is not provided, the resolver first tries the preferred
     * {@code config/releases/{target}/target.yml} convention and then falls back
     * to the legacy {@code config/targets/{target}.yml} convention.</p>
     *
     * <p>Explicit paths are resolved against the project root when they are
     * relative. Absolute explicit paths are preserved as-is.</p>
     *
     * @param projectRoot root directory of the consuming project.
     * @param targetName selected target name.
     * @param explicitTargetConfigPath optional explicit target configuration
     *                                 file path.
     * @param explicitSecretsPath optional explicit secrets file path.
     * @return resolved configuration layer paths.
     * @throws FrameworkException if project root, target name, or target config
     *                            file resolution is invalid.
     */
    public TargetConfigPaths resolve(
            Path projectRoot,
            String targetName,
            Path explicitTargetConfigPath,
            Path explicitSecretsPath
    ) {
        Path normalizedRoot = normalizeProjectRoot(projectRoot);
        String normalizedTarget = normalizeTargetName(targetName);

        Path targetConfigPath = resolveTargetConfigPath(
                normalizedRoot,
                normalizedTarget,
                explicitTargetConfigPath
        );

        return new TargetConfigPaths(
                normalizedTarget,
                targetConfigPath,
                normalizedRoot.resolve(DEFAULT_SECRETS_FILE),
                normalizedRoot.resolve(TARGET_SECRETS_DIRECTORY).resolve(normalizedTarget + ".yml"),
                resolveOptionalPath(normalizedRoot, explicitSecretsPath)
        );
    }

    /**
     * Resolves the target configuration file path using explicit override,
     * preferred convention, and legacy fallback.
     *
     * @param projectRoot normalized project root path.
     * @param targetName normalized target name.
     * @param explicitTargetConfigPath optional explicit target configuration
     *                                 file path.
     * @return resolved existing target configuration file.
     */
    private Path resolveTargetConfigPath(
            Path projectRoot,
            String targetName,
            Path explicitTargetConfigPath
    ) {
        List<Path> attemptedPaths = new ArrayList<>();

        if (explicitTargetConfigPath != null) {
            Path resolvedExplicitPath = resolveOptionalPath(projectRoot, explicitTargetConfigPath);
            attemptedPaths.add(resolvedExplicitPath);

            if (Files.isRegularFile(resolvedExplicitPath)) {
                return resolvedExplicitPath;
            }

            throw new FrameworkException(
                    "Explicit target configuration file does not exist: "
                            + resolvedExplicitPath
            );
        }

        Path preferredPath = projectRoot
                .resolve(RELEASES_DIRECTORY)
                .resolve(targetName)
                .resolve(TARGET_CONFIG_FILE_NAME)
                .toAbsolutePath()
                .normalize();

        attemptedPaths.add(preferredPath);

        if (Files.isRegularFile(preferredPath)) {
            return preferredPath;
        }

        Path legacyPath = projectRoot
                .resolve(TARGETS_DIRECTORY)
                .resolve(targetName + ".yml")
                .toAbsolutePath()
                .normalize();

        attemptedPaths.add(legacyPath);

        if (Files.isRegularFile(legacyPath)) {
            return legacyPath;
        }

        throw new FrameworkException(
                "Target configuration file was not found for target: "
                        + targetName
                        + ". Attempted paths: "
                        + attemptedPaths
        );
    }

    /**
     * Reads a path from a system property.
     *
     * @param propertyName system property name.
     * @return configured path, or {@code null} when the property is absent or
     * blank.
     */
    private Path explicitPathFromSystemProperty(String propertyName) {
        String value = System.getProperty(propertyName);

        if (value == null || value.isBlank()) {
            return null;
        }

        return Path.of(value.trim());
    }

    /**
     * Resolves explicit secrets path from preferred and legacy system
     * properties.
     *
     * <p>The preferred {@code target.secrets.file} property wins over the legacy
     * {@code secrets.file} property when both are present.</p>
     *
     * @return configured explicit secrets path, or {@code null} when no property
     * is set.
     */
    private Path explicitSecretsPathFromSystemProperties() {
        Path preferred = explicitPathFromSystemProperty(TARGET_SECRETS_FILE_PROPERTY);

        if (preferred != null) {
            return preferred;
        }

        return explicitPathFromSystemProperty(LEGACY_SECRETS_FILE_PROPERTY);
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