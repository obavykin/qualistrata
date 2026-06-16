package com.bavykin.qualistrata.unix.ssh.jsch;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.ssh.SshConnection;
import com.bavykin.qualistrata.unix.ssh.SshConnectionRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for the JSch-based SSH connection implementation.
 *
 * <p>These tests require a real SSH host and therefore must not run as part of
 * the regular unit-test build. They are placed under the {@code integrationTest}
 * source set and are guarded by system properties.</p>
 *
 * <p>If the required SSH configuration is not provided, the tests are skipped
 * automatically. This allows {@code ./gradlew integrationTest} to remain green
 * on developer machines and CI agents that do not have SSH test infrastructure.</p>
 */
class JschSshConnectionIntegrationTest {

    private static final String HOST_PROPERTY = "qualistrata.integration.ssh.host";
    private static final String PORT_PROPERTY = "qualistrata.integration.ssh.port";
    private static final String USERNAME_PROPERTY = "qualistrata.integration.ssh.username";
    private static final String PASSWORD_PROPERTY = "qualistrata.integration.ssh.password";
    private static final String KEY_PATH_PROPERTY = "qualistrata.integration.ssh.keyPath";

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Verifies that QualiStrata can open a real SSH connection and execute a
     * simple command through the JSch transport implementation.
     *
     * <p>The test uses only safe read-only commands. It does not modify the
     * remote host.</p>
     */
    @Test
    void shouldConnectToRealSshHostAndExecuteCommand() {
        assumeSshIntegrationConfigurationIsProvided();

        SshConnectionRequest request = new SshConnectionRequest(
                requiredProperty(HOST_PROPERTY),
                optionalPort(),
                requiredProperty(USERNAME_PROPERTY),
                optionalProperty(PASSWORD_PROPERTY),
                optionalProperty(KEY_PATH_PROPERTY),
                CONNECT_TIMEOUT
        );

        try (SshConnection connection = new JschSshConnectionFactory().open(request)) {
            CommandResult result = connection.execute(new UnixCommandRequest(
                    "printf 'qualistrata'",
                    COMMAND_TIMEOUT
            ));

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
                "Skipping SSH integration test because SSH host, username, and authentication are not configured."
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

        assertFalse(value == null || value.isBlank(), "Required property must not be blank: " + name);

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
     * @return configured port, or {@code null} to let the SSH factory use its
     * default port.
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