package com.bavykin.qualistrata.core.config;

import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.resolution.TargetConfigResolutionRequest;
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
 * Unit tests for {@link QualiStrataConfig}.
 *
 * <p>The tests verify the public configuration facade behavior from the point
 * of view of a consuming project. The facade should resolve target
 * configuration, apply generic validation, and return only validated
 * configuration to the caller.</p>
 */
class QualiStrataConfigTest {

    private final QualiStrataConfig config = new QualiStrataConfig();

    @TempDir
    Path projectRoot;

    @Test
    void shouldResolveAndValidateTargetConfiguration() throws Exception {
        createTargetFile("demo", """
                env: demo
                
                ui:
                  baseUrl: "https://app.demo.test"
                
                api: {}
                perf: {}
                hosts: {}
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
        assertEquals("https://app.demo.test", resolvedTarget.config().ui().baseUrl());
    }

    @Test
    void shouldFailWhenResolvedConfigurationIsStructurallyInvalid() throws Exception {
        createTargetFile("demo", """
                env: " "
                
                api: {}
                perf: {}
                hosts: {}
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
    void shouldMergeSecretsBeforeValidation() throws Exception {
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

        createFile("config/secrets/demo.yml", """
                hosts:
                  app:
                    users:
                      app:
                        password: "target-secret"
                """);

        ResolvedTarget resolvedTarget = config.resolveTarget(new TargetConfigResolutionRequest(
                projectRoot,
                new Properties(),
                Map.of(),
                "demo",
                null
        ));

        assertEquals(
                "target-secret",
                resolvedTarget.config().hosts().get("app").users().get("app").password()
        );
    }

    /**
     * Creates a public target configuration file under the conventional target
     * configuration directory.
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