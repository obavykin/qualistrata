package com.bavykin.qualistrata.unix.facade;

import com.bavykin.qualistrata.core.config.model.APIConfig;
import com.bavykin.qualistrata.core.config.model.DBConfig;
import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.PerfConfig;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.config.model.UIConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for the high-level {@link UnixFacade} API over real SSH.
 *
 * <p>The test verifies the full public Unix API chain from resolved
 * {@link TargetConfig} to real remote command execution through the default
 * JSch-backed pooled provider.</p>
 *
 * <p>The test is guarded by system properties and is skipped automatically when
 * real SSH test infrastructure is not configured.</p>
 */
class UnixFacadeSshIntegrationTest {

    private static final String HOST_PROPERTY = "qualistrata.integration.ssh.host";
    private static final String PORT_PROPERTY = "qualistrata.integration.ssh.port";
    private static final String USERNAME_PROPERTY = "qualistrata.integration.ssh.username";
    private static final String PASSWORD_PROPERTY = "qualistrata.integration.ssh.password";
    private static final String KEY_PATH_PROPERTY = "qualistrata.integration.ssh.keyPath";

    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Verifies that the high-level Unix facade can execute a command through
     * the default pooled JSch-backed implementation.
     */
    @Test
    void shouldExecuteCommandThroughHighLevelUnixFacade() {
        assumeSshIntegrationConfigurationIsProvided();

        TargetConfig targetConfig = sshTargetConfig();

        try (UnixFacade unix = UnixFacade.pooled(targetConfig)) {
            CommandResult result = unix.execute(
                    "app",
                    "printf 'qualistrata'",
                    COMMAND_TIMEOUT
            );

            assertEquals(0, result.exitCode());
            assertEquals("qualistrata", result.stdout());
            assertEquals("", result.stderr());
        }
    }

    /**
     * Verifies that the high-level Unix facade can execute a command using an
     * explicit user alias configured under the selected host.
     */
    @Test
    void shouldExecuteCommandThroughHighLevelUnixFacadeWithExplicitUserAlias() {
        assumeSshIntegrationConfigurationIsProvided();

        TargetConfig targetConfig = sshTargetConfig();

        try (UnixFacade unix = UnixFacade.pooled(targetConfig)) {
            CommandResult result = unix.execute(
                    "app",
                    "ssh",
                    "printf 'qualistrata'",
                    COMMAND_TIMEOUT
            );

            assertEquals(0, result.exitCode());
            assertEquals("qualistrata", result.stdout());
            assertEquals("", result.stderr());
        }
    }

    /**
     * Skips the test when no real SSH target is configured.
     */
    private void assumeSshIntegrationConfigurationIsProvided() {
        boolean hasHost = isNotBlank(optionalProperty(HOST_PROPERTY));
        boolean hasUsername = isNotBlank(optionalProperty(USERNAME_PROPERTY));
        boolean hasPassword = isNotBlank(optionalProperty(PASSWORD_PROPERTY));
        boolean hasKeyPath = isNotBlank(optionalProperty(KEY_PATH_PROPERTY));

        assumeTrue(
                hasHost && hasUsername && (hasPassword || hasKeyPath),
                "Skipping UnixFacade SSH integration test because SSH host, username, and authentication are not configured."
        );
    }

    /**
     * Creates a resolved target configuration for SSH integration testing.
     *
     * @return target configuration containing one Unix host alias.
     */
    private TargetConfig sshTargetConfig() {
        return new TargetConfig(
                "integration",
                Map.of(
                        "app",
                        sshHostConfig()
                ),
                new APIConfig(),
                new DBConfig(null, null, null, null, null),
                new UIConfig("https://example.test"),
                new PerfConfig()
        );
    }

    /**
     * Creates a host configuration from SSH integration-test system properties.
     *
     * @return host configuration.
     */
    private HostConfig sshHostConfig() {
        return new HostConfig(
                "application",
                requiredProperty(HOST_PROPERTY),
                optionalPort(),
                "ssh",
                Map.of(
                        "ssh",
                        new UserConfig(
                                requiredProperty(USERNAME_PROPERTY),
                                optionalProperty(PASSWORD_PROPERTY),
                                optionalProperty(KEY_PATH_PROPERTY)
                        )
                ),
                null,
                null,
                new UnixPoolConfig(
                        1,
                        60,
                        5
                ),
                10
        );
    }

    /**
     * Reads a required system property.
     *
     * @param name system property name.
     * @return trimmed property value.
     */
    private String requiredProperty(String name) {
        String value = optionalProperty(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required property must not be blank: " + name);
        }

        return value.trim();
    }

    /**
     * Reads an optional system property.
     *
     * @param name system property name.
     * @return trimmed property value, or {@code null} when property is missing
     * or blank.
     */
    private String optionalProperty(String name) {
        String value = System.getProperty(name);

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    /**
     * Resolves optional SSH port.
     *
     * @return configured port, or {@code null} to use the default SSH port.
     */
    private Integer optionalPort() {
        String value = optionalProperty(PORT_PROPERTY);

        if (value == null) {
            return null;
        }

        return Integer.parseInt(value);
    }

    /**
     * Checks whether a string contains non-blank text.
     *
     * @param value value to check.
     * @return {@code true} when value is not blank.
     */
    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}