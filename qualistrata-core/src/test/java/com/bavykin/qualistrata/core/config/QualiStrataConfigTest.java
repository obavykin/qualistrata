package com.bavykin.qualistrata.core.config;

import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.resolution.TargetConfigResolutionRequest;
import com.bavykin.qualistrata.core.config.secrets.UnixAccessSecret;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link QualiStrataConfig}.
 *
 * <p>The tests verify the public configuration facade behavior from the point
 * of view of a consuming project. The facade resolves target resources,
 * resolves secrets as a separate model, validates only generic target topology,
 * and returns the validated {@link ResolvedTarget}.</p>
 */
class QualiStrataConfigTest {

    private final QualiStrataConfig config = new QualiStrataConfig();

    @TempDir
    Path projectRoot;

    @Test
    void shouldResolveAndValidateTargetConfiguration() throws Exception {
        createTargetFile("demo", """
                env: demo
                resources:
                  ui:
                    web:
                      type: web-ui
                      baseUrl: "https://app.demo.test"
                      defaultAccess: admin
                """);

        ResolvedTarget resolvedTarget = config.resolveTarget(new TargetConfigResolutionRequest(
                projectRoot,
                new Properties(),
                Map.of(),
                "demo",
                null
        ));

        assertEquals("demo", resolvedTarget.name());
        assertEquals("demo", resolvedTarget.config().env());
        assertEquals("https://app.demo.test", resolvedTarget.config().resources().ui().get("web").baseUrl());
    }

    @Test
    void shouldFailWhenResolvedConfigurationIsStructurallyInvalid() throws Exception {
        createTargetFile("demo", """
                env: " "
                resources: {}
                """);

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> config.resolveTarget(new TargetConfigResolutionRequest(
                        projectRoot,
                        new Properties(),
                        Map.of(),
                        "demo",
                        null
                ))
        );

        assertEquals("Target environment name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldLoadSecretsSeparatelyFromTargetResources() throws Exception {
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

        ResolvedTarget resolvedTarget = config.resolveTarget(new TargetConfigResolutionRequest(
                projectRoot,
                new Properties(),
                Map.of(),
                "demo",
                null
        ));

        UnixAccessSecret access = resolvedTarget
                .secrets()
                .access()
                .unix()
                .get("app")
                .get("app-user");

        assertNotNull(resolvedTarget.config().resources().unix().get("app"));
        assertEquals("app.demo.test", resolvedTarget.config().resources().unix().get("app").host());
        assertEquals("app", access.username());
        assertEquals("target-secret", access.password());
    }

    /**
     * Creates a public target configuration file under the conventional target
     * configuration directory.
     */
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
