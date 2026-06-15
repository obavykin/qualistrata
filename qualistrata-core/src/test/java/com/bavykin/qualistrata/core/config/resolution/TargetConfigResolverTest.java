package com.bavykin.qualistrata.core.config.resolution;

import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.core.config.selection.TargetSelector;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link TargetConfigResolver}.
 *
 * <p>The tests verify the generic layered configuration resolution flow. They
 * intentionally use temporary files and demo target names so that the core
 * module does not depend on repository-level configuration files or
 * product-specific environments.</p>
 */
class TargetConfigResolverTest {

    private final TargetConfigResolver resolver = new TargetConfigResolver();

    @TempDir
    Path projectRoot;

    @Test
    void shouldResolveTargetConfigurationFromProjectDefaultAndPublicTargetFile() throws Exception {
        createTargetFile("demo", """
                env: demo
                
                ui:
                  baseUrl: "https://app.demo.test"
                
                api: {}
                perf: {}
                
                db:
                  host: "db.demo.test"
                  port: 1521
                  sid: "DEMO"
                  service:
                  username: "demo_user"
                
                hosts: {}
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
        assertEquals("https://app.demo.test", resolved.config().ui().baseUrl());
        assertEquals("db.demo.test", resolved.config().db().host());
        assertEquals(1521, resolved.config().db().port());
    }

    @Test
    void shouldUseSystemPropertyBeforeProjectDefault() throws Exception {
        createTargetFile("from-property", """
                env: from-property
                api: {}
                perf: {}
                hosts: {}
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
                
                api: {}
                perf: {}
                
                hosts:
                  app:
                    role: "application"
                    host: "app.demo.test"
                    port: 22
                    defaultUser: "app"
                    users:
                      app:
                        username: "app"
                        keyPath: "/home/app/.ssh/id_rsa"
                """);

        createFile("config/secrets.yml", """
                hosts:
                  app:
                    users:
                      app:
                        password: "default-secret"
                """);

        createFile("config/secrets/demo.yml", """
                hosts:
                  app:
                    users:
                      app:
                        password: "target-secret"
                """);

        ResolvedTarget resolved = resolver.resolve(new TargetConfigResolutionRequest(
                projectRoot,
                new Properties(),
                Map.of(),
                "demo",
                null
        ));

        UserConfig user = resolved.config()
                .hosts()
                .get("app")
                .users()
                .get("app");

        assertEquals("app", user.username());
        assertEquals("/home/app/.ssh/id_rsa", user.keyPath());
        assertEquals("target-secret", user.password());
    }

    @Test
    void shouldMergeExplicitSecretsFileAfterConventionalSecrets() throws Exception {
        createTargetFile("demo", """
                env: demo
                api: {}
                perf: {}
                hosts:
                  app:
                    role: "application"
                    host: "app.demo.test"
                    port: 22
                    defaultUser: "app"
                    users:
                      app:
                        username: "app"
                """);

        createFile("config/secrets/demo.yml", """
                hosts:
                  app:
                    users:
                      app:
                        password: "target-secret"
                """);

        Path explicitSecrets = createFile("local/explicit-secrets.yml", """
                hosts:
                  app:
                    users:
                      app:
                        password: "explicit-secret"
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
                resolved.config().hosts().get("app").users().get("app").password()
        );
    }

    @Test
    void shouldSkipMissingOptionalSecretLayers() throws Exception {
        createTargetFile("demo", """
                env: demo
                api: {}
                perf: {}
                hosts: {}
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

        assertEquals(
                "Target configuration file does not exist: "
                        + projectRoot.resolve("config/targets/missing.yml").toAbsolutePath().normalize(),
                exception.getMessage()
        );
    }

    /**
     * Creates a public target configuration file for the given target name.
     *
     * @param targetName target name used in the conventional path.
     * @param content YAML file content.
     * @return created file path.
     * @throws Exception if the file cannot be created.
     */
    private Path createTargetFile(String targetName, String content) throws Exception {
        return createFile("config/targets/" + targetName + ".yml", content);
    }

    /**
     * Creates a file under the temporary project root.
     *
     * @param relativePath path relative to the temporary project root.
     * @param content file content.
     * @return created file path.
     * @throws Exception if the file cannot be created.
     */
    private Path createFile(String relativePath, String content) throws Exception {
        Path file = projectRoot.resolve(relativePath);

        Files.createDirectories(file.getParent());
        Files.writeString(file, content);

        return file;
    }
}