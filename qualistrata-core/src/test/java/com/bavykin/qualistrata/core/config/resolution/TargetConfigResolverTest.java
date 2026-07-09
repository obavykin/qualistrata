package com.bavykin.qualistrata.core.config.resolution;

import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.secrets.UnixAccessSecret;
import com.bavykin.qualistrata.core.config.selection.TargetSelector;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TargetConfigResolver}.
 *
 * <p>The tests verify the generic resolution flow where target topology and
 * access secrets are loaded as separate models. They intentionally use demo
 * aliases so that core remains reusable for different consumer projects.</p>
 */
class TargetConfigResolverTest {

    private final TargetConfigResolver resolver = new TargetConfigResolver();

    @TempDir
    Path projectRoot;

    @Test
    void shouldResolveTargetConfigurationFromProjectDefaultAndPublicTargetFile() throws Exception {
        createTargetFile("demo", """
                env: demo
                resources:
                  ui:
                    web:
                      type: web-ui
                      baseUrl: "https://app.demo.test"
                      defaultAccess: admin
                  databases:
                    main:
                      type: oracle
                      host: "db.demo.test"
                      port: 1521
                      databaseName: DEMO
                      connectionMode: sid
                      defaultAccess: app-db-user
                """);

        ResolvedTarget resolved = resolver.resolve(new TargetConfigResolutionRequest(
                projectRoot,
                new Properties(),
                Map.of(),
                "demo",
                null
        ));

        assertEquals("demo", resolved.name());
        assertEquals("demo", resolved.config().env());
        assertEquals("https://app.demo.test", resolved.config().resources().ui().get("web").baseUrl());
        assertEquals("db.demo.test", resolved.config().resources().databases().get("main").host());
        assertEquals(1521, resolved.config().resources().databases().get("main").port());
    }

    @Test
    void shouldUseSystemPropertyBeforeProjectDefault() throws Exception {
        createTargetFile("from-property", """
                env: from-property
                resources: {}
                """);

        Properties properties = new Properties();
        properties.setProperty(TargetSelector.TARGET_SYSTEM_PROPERTY, "from-property");

        ResolvedTarget resolved = resolver.resolve(new TargetConfigResolutionRequest(
                projectRoot,
                properties,
                Map.of(),
                "from-default",
                null
        ));

        assertEquals("from-property", resolved.name());
        assertEquals("from-property", resolved.config().env());
    }

    @Test
    void shouldMergeDefaultAndTargetSpecificSecrets() throws Exception {
        createTargetFile("demo", """
                env: demo
                resources:
                  unix:
                    app:
                      role: application
                      host: "app.demo.test"
                      port: 22
                      defaultAccess: app-user
                """);

        createFile("config/secrets.yml", """
                access:
                  unix:
                    app:
                      app-user:
                        username: app
                        password: default-secret
                """);

        createFile("config/secrets/demo.yml", """
                access:
                  unix:
                    app:
                      app-user:
                        username: app
                        password: target-secret
                """);

        ResolvedTarget resolved = resolver.resolve(new TargetConfigResolutionRequest(
                projectRoot,
                new Properties(),
                Map.of(),
                "demo",
                null
        ));

        UnixAccessSecret access = resolved.secrets().access().unix().get("app").get("app-user");

        assertEquals("app", access.username());
        assertEquals("target-secret", access.password());
    }

    @Test
    void shouldMergeExplicitSecretsFileAfterConventionalSecrets() throws Exception {
        createTargetFile("demo", """
                env: demo
                resources:
                  unix:
                    app:
                      role: application
                      host: "app.demo.test"
                      port: 22
                      defaultAccess: app-user
                """);

        createFile("config/secrets/demo.yml", """
                access:
                  unix:
                    app:
                      app-user:
                        username: app
                        password: target-secret
                """);

        Path explicitSecrets = createFile("local/explicit-secrets.yml", """
                access:
                  unix:
                    app:
                      app-user:
                        username: app
                        password: explicit-secret
                """);

        ResolvedTarget resolved = resolver.resolve(new TargetConfigResolutionRequest(
                projectRoot,
                new Properties(),
                Map.of(),
                "demo",
                explicitSecrets
        ));

        assertEquals(
                "explicit-secret",
                resolved.secrets().access().unix().get("app").get("app-user").password()
        );
    }

    @Test
    void shouldSkipMissingOptionalSecretLayers() throws Exception {
        createTargetFile("demo", """
                env: demo
                resources: {}
                """);

        ResolvedTarget resolved = resolver.resolve(new TargetConfigResolutionRequest(
                projectRoot,
                new Properties(),
                Map.of(),
                "demo",
                null
        ));

        assertEquals("demo", resolved.name());
        assertEquals("demo", resolved.config().env());
        assertNull(resolved.secrets());
    }

    @Test
    void shouldFailWhenRequestIsNull() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> resolver.resolve(null)
        );

        assertEquals(
                "Target configuration resolution request must not be null.",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenMandatoryTargetFileDoesNotExist() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> resolver.resolve(new TargetConfigResolutionRequest(
                        projectRoot,
                        new Properties(),
                        Map.of(),
                        "missing",
                        null
                ))
        );

        assertTrue(exception.getMessage().contains(
                "Target configuration file was not found for target: missing."
        ));

        assertTrue(exception.getMessage().contains(
                projectRoot.resolve("config/releases/missing/target.yml")
                        .toAbsolutePath()
                        .normalize()
                        .toString()
        ));

        assertTrue(exception.getMessage().contains(
                projectRoot.resolve("config/targets/missing.yml")
                        .toAbsolutePath()
                        .normalize()
                        .toString()
        ));
    }

    /** Creates a public target configuration file for the given target name. */
    private Path createTargetFile(String targetName, String content) throws Exception {
        return createFile("config/targets/" + targetName + ".yml", content);
    }

    /** Creates a file under the temporary project root. */
    private Path createFile(String relativePath, String content) throws Exception {
        Path file = projectRoot.resolve(relativePath);

        Files.createDirectories(file.getParent());
        Files.writeString(file, content);

        return file;
    }
}
