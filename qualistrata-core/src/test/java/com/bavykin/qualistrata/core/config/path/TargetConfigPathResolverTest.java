package com.bavykin.qualistrata.core.config.path;

import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link TargetConfigPathResolver}.
 *
 * <p>The tests document the generic path convention used by QualiStrata core.
 * The resolver must build predictable paths without loading files or embedding
 * product-specific target names.</p>
 */
class TargetConfigPathResolverTest {

    private final TargetConfigPathResolver resolver = new TargetConfigPathResolver();

    @TempDir
    Path projectRoot;

    @Test
    void shouldResolveConventionalTargetConfigurationPaths() {
        TargetConfigPaths paths = resolver.resolve(projectRoot, "demo");

        assertEquals("demo", paths.targetName());
        assertEquals(
                projectRoot.resolve("config/targets/demo.yml").toAbsolutePath().normalize(),
                paths.targetConfigPath()
        );
        assertEquals(
                projectRoot.resolve("config/secrets.yml").toAbsolutePath().normalize(),
                paths.defaultSecretsPath()
        );
        assertEquals(
                projectRoot.resolve("config/secrets/demo.yml").toAbsolutePath().normalize(),
                paths.targetSecretsPath()
        );
        assertNull(paths.explicitSecretsPath());
    }

    @Test
    void shouldTrimTargetNameBeforeResolvingPaths() {
        TargetConfigPaths paths = resolver.resolve(projectRoot, "  demo  ");

        assertEquals("demo", paths.targetName());
        assertEquals(
                projectRoot.resolve("config/targets/demo.yml").toAbsolutePath().normalize(),
                paths.targetConfigPath()
        );
    }

    @Test
    void shouldResolveRelativeExplicitSecretsPathAgainstProjectRoot() {
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
    void shouldPreserveAbsoluteExplicitSecretsPath() {
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
}