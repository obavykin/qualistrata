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
 * <p>The tests validate the new generic resource topology structure. Access
 * secrets are intentionally not validated here because they are resolved later
 * by the access resolver when a concrete runtime client or session is needed.</p>
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
    void shouldFailWhenResourcesSectionIsMissing() throws Exception {
        TargetConfig config = load("""
                env: demo
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Target configuration must contain resources section.", exception.getMessage());
    }

    @Test
    void shouldFailWhenUiBaseUrlIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  ui:
                    web:
                      type: web-ui
                      baseUrl: " "
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("UI base URL must not be blank for alias: web", exception.getMessage());
    }

    @Test
    void shouldFailWhenUnixHostAliasIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  unix:
                    " ":
                      role: application
                      host: "app.demo.test"
                      port: 22
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Unix resource alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenUnixHostAddressIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  unix:
                    app:
                      role: application
                      host: " "
                      port: 22
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Unix host address must not be blank for alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenUnixHostPortIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  unix:
                    app:
                      role: application
                      host: "app.demo.test"
                      port: -1
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Unix host port must be positive for alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenUnixPoolSizeIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  unix:
                    app:
                      role: application
                      host: "app.demo.test"
                      port: 22
                      pool:
                        size: 0
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Unix pool size must be positive for alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenUnixPoolKeepAliveIsNegative() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  unix:
                    app:
                      role: application
                      host: "app.demo.test"
                      port: 22
                      pool:
                        keepAliveSeconds: -1
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Unix pool keep-alive seconds must be non-negative for alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenUnixPoolBorrowTimeoutIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  unix:
                    app:
                      role: application
                      host: "app.demo.test"
                      port: 22
                      pool:
                        borrowTimeoutSeconds: 0
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Unix pool borrow timeout seconds must be positive for alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenUnixCommandTimeoutIsNotPositive() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  unix:
                    app:
                      role: application
                      host: "app.demo.test"
                      port: 22
                      commandTimeoutSeconds: 0
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Unix command timeout must be positive for alias: app", exception.getMessage());
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
                      databaseName: DEMO
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Database port must be positive for alias: main", exception.getMessage());
    }

    @Test
    void shouldFailWhenDatabaseTypeIsBlank() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  databases:
                    main:
                      type: " "
                      host: "db.demo.test"
                      port: 1521
                      databaseName: DEMO
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Database type must not be blank for alias: main", exception.getMessage());
    }

    @Test
    void shouldFailWhenDatabaseHostIsBlankAndJdbcUrlIsMissing() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  databases:
                    main:
                      type: oracle
                      host: " "
                      port: 1521
                      databaseName: DEMO
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Database host must not be blank for alias: main when jdbcUrl is not configured.", exception.getMessage());
    }

    @Test
    void shouldAcceptDatabaseWithJdbcUrlWithoutHostAndDatabaseName() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  databases:
                    main:
                      type: postgresql
                      jdbcUrl: "jdbc:postgresql://db.demo.test:5432/demo"
                """);

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void shouldFailWhenEnabledMetricsResourceHasNoUrl() throws Exception {
        TargetConfig config = load("""
                env: demo
                resources:
                  metrics:
                    influx:
                      type: influxdb
                      enabled: true
                      url: " "
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> validator.validate(config)
        );

        assertEquals("Metrics URL must not be blank for enabled alias: influx", exception.getMessage());
    }

    /** Creates a temporary YAML file and loads it as {@link TargetConfig}. */
    private TargetConfig load(String content) throws Exception {
        Path file = tempDir.resolve("target.yml");

        Files.writeString(file, content);

        return loader.load(file);
    }
}
