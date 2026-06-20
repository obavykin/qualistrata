package com.bavykin.qualistrata.core.config.loader;

import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link TargetConfigLoader}.
 *
 * <p>The tests verify the new generic resource topology model. They avoid
 * project-specific aliases and intentionally do not use the removed legacy
 * root-level {@code ui}, {@code db}, and {@code hosts} sections.</p>
 */
class TargetConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldLoadTargetConfigurationFromYamlFile() throws Exception {
        Path configFile = tempDir.resolve("target.yml");

        Files.writeString(configFile, """
                env: demo
                resources:
                  ui:
                    web:
                      type: web-ui
                      baseUrl: "https://app.demo.test"
                      defaultAccess: admin
                  unix:
                    app:
                      role: application
                      host: "app.demo.test"
                      port: 22
                      defaultAccess: app-user
                      commandTimeoutSeconds: 60
                  databases:
                    main:
                      type: oracle
                      host: "db.demo.test"
                      port: 1521
                      databaseName: DEMO
                      connectionMode: sid
                      defaultAccess: app-db-user
                      connectionTimeoutSeconds: 30
                      queryTimeoutSeconds: 30
                  api:
                    public-api:
                      type: rest
                      baseUrl: "https://api.demo.test"
                      defaultAccess: service-account
                  metrics:
                    influx:
                      type: influxdb
                      enabled: false
                      url: "https://metrics.demo.test"
                      org: demo
                      bucket: automation
                      defaultAccess: writer
                """);

        TargetConfig config = new TargetConfigLoader().load(configFile);

        assertEquals("demo", config.env());
        assertNotNull(config.resources());
        assertEquals("https://app.demo.test", config.resources().ui().get("web").baseUrl());
        assertEquals("app.demo.test", config.resources().unix().get("app").host());
        assertEquals(22, config.resources().unix().get("app").port());
        assertEquals("db.demo.test", config.resources().databases().get("main").host());
        assertEquals(1521, config.resources().databases().get("main").port());
        assertEquals("DEMO", config.resources().databases().get("main").databaseName());
        assertEquals("rest", config.resources().api().get("public-api").type());
        assertEquals("influxdb", config.resources().metrics().get("influx").type());
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
