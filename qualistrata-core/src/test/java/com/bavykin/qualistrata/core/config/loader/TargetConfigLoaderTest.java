package com.bavykin.qualistrata.core.config.loader;

import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link TargetConfigLoader}.
 *
 * <p>The tests verify only generic YAML loading behavior. They intentionally do
 * not rely on repository-level configuration files or product-specific target
 * names, so the core module remains reusable across different projects.</p>
 */
class TargetConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldLoadTargetConfigurationFromYamlFile() throws Exception {
        Path configFile = tempDir.resolve("target.yml");
        Files.writeString(configFile, validTargetYaml());

        TargetConfig config = new TargetConfigLoader().load(configFile);

        assertEquals("PT-A", config.env());
        assertEquals("release_6.6", config.release());
        assertEquals("https://app.demo.test", config.resources().ui().get("tracker").baseUrl());
        assertEquals("app.demo.test", config.resources().unix().get("app").host());
        assertEquals("/opt/tracker/.profile", config.resources().unix().get("app").shellProfile());
        assertEquals("db.demo.test", config.resources().databases().get("main").host());
        assertEquals(1521, config.resources().databases().get("main").port());
        assertEquals("DEMO", config.resources().databases().get("main").databaseName());
    }

    @Test
    void shouldRetryUntilInitiallyEmptyConfigurationBecomesReadable() throws Exception {
        Path configFile = tempDir.resolve("generated-target.yml");
        Files.writeString(configFile, "");

        CompletableFuture<Void> writer = CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(75);
                Files.writeString(configFile, validTargetYaml());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        TargetConfigLoader loader = new TargetConfigLoader(
                new ObjectMapper(new YAMLFactory()),
                20,
                Duration.ofMillis(20)
        );

        TargetConfig config = loader.load(configFile);
        writer.get(1, TimeUnit.SECONDS);

        assertEquals("PT-A", config.env());
        assertEquals("release_6.6", config.release());
    }

    @Test
    void shouldFailAfterConfiguredAttemptsWhenConfigurationRemainsEmpty() throws Exception {
        Path configFile = tempDir.resolve("empty-target.yml");
        Files.writeString(configFile, "");

        TargetConfigLoader loader = new TargetConfigLoader(
                new ObjectMapper(new YAMLFactory()),
                3,
                Duration.ZERO
        );

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> loader.load(configFile)
        );

        assertEquals(
                "Failed to load target configuration from: " + configFile
                        + " after 3 attempts.",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenPathIsNull() {
        TargetConfigLoader loader = new TargetConfigLoader();

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> loader.load(null)
        );

        assertEquals("Target configuration path must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenFileDoesNotExist() {
        Path missingFile = tempDir.resolve("missing.yml");

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> new TargetConfigLoader().load(missingFile)
        );

        assertEquals("Target configuration file does not exist: " + missingFile, exception.getMessage());
    }

    private static String validTargetYaml() {
        return """
                env: PT-A
                release: release_6.6
                resources:
                  ui:
                    tracker:
                      type: web-ui
                      baseUrl: "https://app.demo.test"
                      defaultAccess: admin
                  unix:
                    app:
                      role: application
                      host: "app.demo.test"
                      port: 22
                      defaultAccess: tracker
                      commandTimeoutSeconds: 60
                      shellProfile: "/opt/tracker/.profile"
                  databases:
                    main:
                      type: oracle
                      host: "db.demo.test"
                      port: 1521
                      databaseName: "DEMO"
                      connectionMode: sid
                      defaultAccess: sagsys
                      connectionTimeoutSeconds: 10
                      queryTimeoutSeconds: 30
                  api: {}
                  metrics: {}
                """;
    }
}
