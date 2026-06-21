package com.bavykin.qualistrata.core.config.validation;

import com.bavykin.qualistrata.core.config.loader.TargetConfigLoader;
import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link TargetConfigValidator}.
 *
 * <p>The tests use YAML files loaded through {@link TargetConfigLoader} to
 * validate the same resource-based model shape that consuming projects receive
 * after configuration loading and resolution.</p>
 */
class TargetConfigValidatorTest {

    private final TargetConfigLoader loader = new TargetConfigLoader();
    private final TargetConfigValidator validator = new TargetConfigValidator();

    @TempDir
    Path tempDir;

    @Test
    void shouldAcceptMinimalValidTargetConfiguration() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources: {}
                """);

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void shouldAcceptValidResolvedTarget() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources: {}
                """);

        ResolvedTarget resolvedTarget = new ResolvedTarget("demo", config, null);

        assertDoesNotThrow(() -> validator.validate(resolvedTarget));
    }

    @Test
    void shouldFailWhenResolvedTargetIsNull() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate((ResolvedTarget) null)
        );

        assertEquals("Resolved target must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenResolvedTargetNameIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources: {}
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(new ResolvedTarget("   ", config, null))
        );

        assertEquals("Resolved target name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenResolvedTargetConfigIsNull() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(new ResolvedTarget("demo", null, null))
        );

        assertEquals("Resolved target configuration must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenTargetConfigurationIsNull() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate((TargetConfig) null)
        );

        assertEquals("Target configuration must not be null.", exception.getMessage());
    }

    @Test
    void shouldAcceptConfiguredReleaseIdentifier() throws Exception {
        TargetConfig config = load("""
                env: PT-A
                release: release_6.6
                resources: {}
                """);

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void shouldFailWhenReleaseIdentifierIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                release: " "
                resources: {}
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Target release identifier must not be blank when configured.", exception.getMessage());
    }

    @Test
    void shouldFailWhenEnvironmentNameIsBlank() throws Exception {
        TargetConfig config = load("""
                env: " "
                resources: {}
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Target environment name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenUiResourceAliasIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  ui:
                    " ":
                      baseUrl: "https://demo.test"
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("UI resource alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenUiBaseUrlIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  ui:
                    tracker:
                      baseUrl: " "
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("UI base URL must not be blank for resource alias: tracker", exception.getMessage());
    }

    @Test
    void shouldFailWhenUnixHostAddressIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  unix:
                    app:
                      host: " "
                      port: 22
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Unix host address must not be blank for resource alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenUnixPortIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  unix:
                    app:
                      host: "app.demo.test"
                      port: 0
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Unix host port must be positive for resource alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenDatabasePortIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  databases:
                    main:
                      type: oracle
                      host: "db.demo.test"
                      port: 0
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Database port must be positive for resource alias: main", exception.getMessage());
    }

    @Test
    void shouldFailWhenDatabasePoolSizeIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  databases:
                    main:
                      type: oracle
                      host: "db.demo.test"
                      port: 1521
                      pool:
                        size: 0
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Database pool size must be positive for resource alias: main", exception.getMessage());
    }

    private TargetConfig load(String yaml) throws Exception {
        Path file = tempDir.resolve("target.yml");
        Files.writeString(file, yaml);
        return loader.load(file);
    }
}
