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
 * validate the same model shape that consuming projects will receive after
 * configuration loading and resolution.</p>
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
                api: {}
                perf: {}
                hosts: {}
                """);

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void shouldAcceptValidResolvedTarget() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                hosts: {}
                """);

        ResolvedTarget resolvedTarget = new ResolvedTarget("demo", config);

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
                api: {}
                perf: {}
                hosts: {}
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(new ResolvedTarget("   ", config))
        );

        assertEquals("Resolved target name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenResolvedTargetConfigIsNull() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(new ResolvedTarget("demo", null))
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
    void shouldFailWhenEnvironmentNameIsBlank() throws Exception {
        TargetConfig config = load("""
                env: " "
                api: {}
                perf: {}
                hosts: {}
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Target environment name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenUiBaseUrlIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                
                ui:
                  baseUrl: " "
                
                api: {}
                perf: {}
                hosts: {}
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals(
                "UI base URL must not be blank when UI section is configured.",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenDatabasePortIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                
                api: {}
                perf: {}
                
                db:
                  host: "db.demo.test"
                  port: 0
                
                hosts: {}
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Database port must be positive when configured.", exception.getMessage());
    }

    @Test
    void shouldAcceptHostWithoutUsers() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                
                hosts:
                  app:
                    role: "application"
                    host: "app.demo.test"
                    port: 22
                """);

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void shouldFailWhenHostAliasIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                
                hosts:
                  " ":
                    role: "application"
                    host: "app.demo.test"
                    port: 22
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Host alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAddressIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                
                hosts:
                  app:
                    role: "application"
                    host: " "
                    port: 22
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Host address must not be blank for alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostPortIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                
                hosts:
                  app:
                    role: "application"
                    host: "app.demo.test"
                    port: -1
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Host port must be positive for alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenDefaultUserDoesNotExist() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                
                hosts:
                  app:
                    role: "application"
                    host: "app.demo.test"
                    port: 22
                    defaultUser: "missing"
                    users:
                      app:
                        username: "app"
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Default user 'missing' is not defined for host alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenUserAliasIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                
                hosts:
                  app:
                    role: "application"
                    host: "app.demo.test"
                    port: 22
                    users:
                      " ":
                        username: "app"
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("User alias must not be blank for host alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenUsernameIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                
                hosts:
                  app:
                    role: "application"
                    host: "app.demo.test"
                    port: 22
                    users:
                      app:
                        username: " "
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals(
                "Username must not be blank for user alias: app on host alias: app",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenPoolSizeIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                
                hosts:
                  app:
                    role: "application"
                    host: "app.demo.test"
                    port: 22
                    pool:
                      size: 0
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Unix pool size must be positive for host alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenPoolKeepAliveIsNegative() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                
                hosts:
                  app:
                    role: "application"
                    host: "app.demo.test"
                    port: 22
                    pool:
                      keepAliveSeconds: -1
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals(
                "Unix pool keep-alive seconds must be non-negative for host alias: app",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenPoolBorrowTimeoutIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                
                hosts:
                  app:
                    role: "application"
                    host: "app.demo.test"
                    port: 22
                    pool:
                      borrowTimeoutSeconds: 0
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals(
                "Unix pool borrow timeout seconds must be positive for host alias: app",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenCommandTimeoutIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                api: {}
                perf: {}
                
                hosts:
                  app:
                    role: "application"
                    host: "app.demo.test"
                    port: 22
                    commandTimeoutSeconds: 0
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Command timeout must be positive for alias: app", exception.getMessage());
    }

    /**
     * Creates a temporary YAML file and loads it as {@link TargetConfig}.
     *
     * @param content YAML content.
     * @return loaded target configuration.
     * @throws Exception if the file cannot be written or loaded.
     */
    private TargetConfig load(String content) throws Exception {
        Path file = tempDir.resolve("target.yml");

        Files.writeString(file, content);

        return loader.load(file);
    }
}