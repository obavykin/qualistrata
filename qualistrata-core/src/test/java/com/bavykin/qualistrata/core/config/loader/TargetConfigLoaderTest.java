package com.bavykin.qualistrata.core.config.loader;

import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

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

        Files.writeString(configFile, """
                env: demo
                                
                ui:
                  baseUrl: "https://app.demo.test"
                                
                db:
                  host: "db.demo.test"
                  port: 1521
                  sid: "DEMO"
                  service:
                  username: "demo_user"
                                
                api: {}
                perf: {}
                hosts: {}
                """);

        TargetConfig config = new TargetConfigLoader().load(configFile);

        assertEquals("demo", config.env());
        assertEquals("https://app.demo.test", config.ui().baseUrl());
        assertEquals("db.demo.test", config.db().host());
        assertEquals(1521, config.db().port());
        assertEquals("DEMO", config.db().sid());
        assertEquals("demo_user", config.db().username());
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
}