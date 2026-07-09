package com.bavykin.qualistrata.core.config.path;

import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TargetConfigPathResolver}.
 *
 * <p>The tests document the generic path convention used by QualiStrata core.
 * The resolver must resolve predictable paths without embedding product-specific
 * target names or consuming-project assumptions.</p>
 */
class TargetConfigPathResolverTest {

    private final TargetConfigPathResolver resolver = new TargetConfigPathResolver();

    @TempDir
    Path projectRoot;

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty(TargetConfigPathResolver.TARGET_CONFIG_FILE_PROPERTY);
        System.clearProperty(TargetConfigPathResolver.TARGET_SECRETS_FILE_PROPERTY);
        System.clearProperty(TargetConfigPathResolver.LEGACY_SECRETS_FILE_PROPERTY);
    }

    @Test
    void shouldUseExplicitTargetConfigFile() throws IOException {
        Path explicitTargetConfig = createFile("custom/path/my-target.yml");

        System.setProperty(
                TargetConfigPathResolver.TARGET_CONFIG_FILE_PROPERTY,
                explicitTargetConfig.toString()
        );

        TargetConfigPaths paths = resolver.resolve(projectRoot, "demo");

        assertEquals("demo", paths.targetName());
        assertEquals(explicitTargetConfig.toAbsolutePath().normalize(), paths.targetConfigPath());
    }

    @Test
    void shouldFailWhenExplicitTargetConfigFileDoesNotExist() {
        Path missingTargetConfig = projectRoot.resolve("custom/path/missing-target.yml");

        System.setProperty(
                TargetConfigPathResolver.TARGET_CONFIG_FILE_PROPERTY,
                missingTargetConfig.toString()
        );

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> resolver.resolve(projectRoot, "demo")
        );

        assertEquals(
                "Explicit target configuration file does not exist: "
                        + missingTargetConfig.toAbsolutePath().normalize(),
                exception.getMessage()
        );
    }

    @Test
    void shouldPreferNewReleaseBasedTargetConfigurationConvention() throws IOException {
        Path preferredTargetConfig = createFile("config/releases/release_6.6/target.yml");

        TargetConfigPaths paths = resolver.resolve(projectRoot, "release_6.6");

        assertEquals("release_6.6", paths.targetName());
        assertEquals(preferredTargetConfig.toAbsolutePath().normalize(), paths.targetConfigPath());
        assertEquals(
                projectRoot.resolve("config/secrets.yml").toAbsolutePath().normalize(),
                paths.defaultSecretsPath()
        );
        assertEquals(
                projectRoot.resolve("config/secrets/release_6.6.yml").toAbsolutePath().normalize(),
                paths.targetSecretsPath()
        );
        assertNull(paths.explicitSecretsPath());
    }

    @Test
    void shouldFallbackToLegacyTargetConfigurationConvention() throws IOException {
        Path legacyTargetConfig = createFile("config/targets/release_6.6.yml");

        TargetConfigPaths paths = resolver.resolve(projectRoot, "release_6.6");

        assertEquals("release_6.6", paths.targetName());
        assertEquals(legacyTargetConfig.toAbsolutePath().normalize(), paths.targetConfigPath());
    }

    @Test
    void shouldPreferNewConventionWhenBothNewAndLegacyFilesExist() throws IOException {
        Path preferredTargetConfig = createFile("config/releases/release_6.6/target.yml");

        createFile("config/targets/release_6.6.yml");

        TargetConfigPaths paths = resolver.resolve(projectRoot, "release_6.6");

        assertEquals(preferredTargetConfig.toAbsolutePath().normalize(), paths.targetConfigPath());
    }

    @Test
    void shouldTrimTargetNameBeforeResolvingPaths() throws IOException {
        Path preferredTargetConfig = createFile("config/releases/demo/target.yml");

        TargetConfigPaths paths = resolver.resolve(projectRoot, "  demo  ");

        assertEquals("demo", paths.targetName());
        assertEquals(preferredTargetConfig.toAbsolutePath().normalize(), paths.targetConfigPath());
    }

    @Test
    void shouldFailWithAttemptedPathsWhenTargetConfigIsMissing() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> resolver.resolve(projectRoot, "release_6.6")
        );

        Path preferredPath = projectRoot
                .resolve("config/releases/release_6.6/target.yml")
                .toAbsolutePath()
                .normalize();

        Path legacyPath = projectRoot
                .resolve("config/targets/release_6.6.yml")
                .toAbsolutePath()
                .normalize();

        assertTrue(exception.getMessage().contains(
                "Target configuration file was not found for target: release_6.6."
        ));
        assertTrue(exception.getMessage().contains(preferredPath.toString()));
        assertTrue(exception.getMessage().contains(legacyPath.toString()));
    }

    @Test
    void shouldResolveRelativeExplicitSecretsPathAgainstProjectRoot() throws IOException {
        createFile("config/releases/demo/target.yml");

        TargetConfigPaths paths = resolver.resolve(
                projectRoot,
                "demo",
                Path.of("local/secrets.yml")
        );

        assertEquals(
                projectRoot.resolve("local/secrets.yml").toAbsolutePath().normalize(),
                paths.explicitSecretsPath()
        );
    }

    @Test
    void shouldPreserveAbsoluteExplicitSecretsPath() throws IOException {
        createFile("config/releases/demo/target.yml");

        Path explicitSecretsPath = projectRoot.resolve("external/secrets.yml")
                .toAbsolutePath()
                .normalize();

        TargetConfigPaths paths = resolver.resolve(
                projectRoot,
                "demo",
                explicitSecretsPath
        );

        assertEquals(explicitSecretsPath, paths.explicitSecretsPath());
    }

    @Test
    void shouldUsePreferredExplicitSecretsProperty() throws IOException {
        createFile("config/releases/demo/target.yml");

        Path preferredSecretsPath = projectRoot.resolve("preferred/secrets.yml");
        System.setProperty(
                TargetConfigPathResolver.TARGET_SECRETS_FILE_PROPERTY,
                preferredSecretsPath.toString()
        );

        TargetConfigPaths paths = resolver.resolve(projectRoot, "demo");

        assertEquals(
                preferredSecretsPath.toAbsolutePath().normalize(),
                paths.explicitSecretsPath()
        );
    }

    @Test
    void shouldPreferTargetSecretsFilePropertyOverLegacySecretsFileProperty() throws IOException {
        createFile("config/releases/demo/target.yml");

        Path preferredSecretsPath = projectRoot.resolve("preferred/secrets.yml");
        Path legacySecretsPath = projectRoot.resolve("legacy/secrets.yml");

        System.setProperty(
                TargetConfigPathResolver.TARGET_SECRETS_FILE_PROPERTY,
                preferredSecretsPath.toString()
        );
        System.setProperty(
                TargetConfigPathResolver.LEGACY_SECRETS_FILE_PROPERTY,
                legacySecretsPath.toString()
        );

        TargetConfigPaths paths = resolver.resolve(projectRoot, "demo");

        assertEquals(
                preferredSecretsPath.toAbsolutePath().normalize(),
                paths.explicitSecretsPath()
        );
    }

    @Test
    void shouldUseLegacyExplicitSecretsProperty() throws IOException {
        createFile("config/releases/demo/target.yml");

        Path legacySecretsPath = projectRoot.resolve("legacy/secrets.yml");
        System.setProperty(
                TargetConfigPathResolver.LEGACY_SECRETS_FILE_PROPERTY,
                legacySecretsPath.toString()
        );

        TargetConfigPaths paths = resolver.resolve(projectRoot, "demo");

        assertEquals(
                legacySecretsPath.toAbsolutePath().normalize(),
                paths.explicitSecretsPath()
        );
    }

    @Test
    void shouldFailWhenProjectRootIsNull() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> resolver.resolve(null, "demo")
        );

        assertEquals("Project root path must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenTargetNameIsNull() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> resolver.resolve(projectRoot, null)
        );

        assertEquals("Target name must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenTargetNameIsBlank() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> resolver.resolve(projectRoot, "   ")
        );

        assertEquals("Target name must not be blank.", exception.getMessage());
    }

    /**
     * Creates a test file under the temporary project root.
     *
     * @param relativePath relative file path.
     * @return created file path.
     * @throws IOException if the file cannot be created.
     */
    private Path createFile(String relativePath) throws IOException {
        Path file = projectRoot.resolve(relativePath);

        Files.createDirectories(file.getParent());
        Files.writeString(file, "test: value");

        return file;
    }
}