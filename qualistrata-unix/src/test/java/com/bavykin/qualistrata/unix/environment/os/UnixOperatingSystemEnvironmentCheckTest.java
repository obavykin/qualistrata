package com.bavykin.qualistrata.unix.environment.os;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSeverity;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckStatus;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.facade.UnixFacade;
import com.bavykin.qualistrata.unix.session.UnixHostSession;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionException;
import com.bavykin.qualistrata.unix.session.UnixSessionProvider;
import com.bavykin.qualistrata.unix.session.request.UnixHostSessionRequestFactory;
import com.bavykin.qualistrata.unix.testsupport.UnixTestTargets;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link UnixOperatingSystemEnvironmentCheck}.
 */
class UnixOperatingSystemEnvironmentCheckTest {

    @Test
    void shouldPassWhenOsMatchesExpectedProfile() {
        RecordingUnixFacade unix = new RecordingUnixFacade(osReleaseResult("rhel", "9.5"));

        UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
                unix,
                "unix.app.os",
                "Unix app OS",
                EnvironmentCheckSeverity.ERROR,
                "app",
                new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals(EnvironmentCheckSeverity.INFO, result.severity());
        assertEquals("Unix operating system baseline matches expected profile.", result.message());
        assertEquals("app", result.details().get("hostAlias"));
        assertEquals("cat /etc/os-release", result.details().get("command"));
        assertEquals("rhel", result.details().get("actualId"));
        assertEquals("9.5", result.details().get("actualVersionId"));
        assertNotNull(result.duration());
    }

    @Test
    void shouldPassWhenOnlyMajorVersionIsExpected() {
        RecordingUnixFacade unix = new RecordingUnixFacade(osReleaseResult("rhel", "9.5"));

        UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
                unix,
                "unix.app.os",
                "Unix app OS",
                EnvironmentCheckSeverity.ERROR,
                "app",
                new UnixOperatingSystemProfile("rhel", 9, null),
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
    }

    @Test
    void shouldFailWhenOsIdDoesNotMatch() {
        RecordingUnixFacade unix = new RecordingUnixFacade(osReleaseResult("ubuntu", "24.04"));

        UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
                unix,
                "unix.app.os",
                "Unix app OS",
                EnvironmentCheckSeverity.ERROR,
                "app",
                new UnixOperatingSystemProfile("rhel", null, null),
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(
                "Unexpected OS id. Actual: ubuntu, expected: rhel",
                result.message()
        );
    }

    @Test
    void shouldFailWhenExactVersionIdDoesNotMatch() {
        RecordingUnixFacade unix = new RecordingUnixFacade(osReleaseResult("rhel", "9.4"));

        UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
                unix,
                "unix.app.os",
                "Unix app OS",
                EnvironmentCheckSeverity.ERROR,
                "app",
                new UnixOperatingSystemProfile("rhel", null, "9.5"),
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(
                "Unexpected OS VERSION_ID. Actual: 9.4, expected: 9.5",
                result.message()
        );
    }

    @Test
    void shouldFailWhenMajorVersionDoesNotMatch() {
        RecordingUnixFacade unix = new RecordingUnixFacade(osReleaseResult("rhel", "8.10"));

        UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
                unix,
                "unix.app.os",
                "Unix app OS",
                EnvironmentCheckSeverity.ERROR,
                "app",
                new UnixOperatingSystemProfile("rhel", 9, null),
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(
                "Unexpected OS major version. Actual: 8, expected: 9",
                result.message()
        );
    }

    @Test
    void shouldReturnSkippedWhenExpectedProfileIsMissing() {
        RecordingUnixFacade unix = new RecordingUnixFacade(osReleaseResult("rhel", "9.5"));

        UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
                unix,
                "unix.app.os",
                "Unix app OS",
                EnvironmentCheckSeverity.WARNING,
                "app",
                null,
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.SKIPPED, result.status());
        assertEquals(EnvironmentCheckSeverity.WARNING, result.severity());
        assertEquals("Unix operating system baseline is not defined.", result.message());
        assertEquals("app", result.details().get("hostAlias"));
    }

    @Test
    void shouldFailWhenOsReleaseCommandFails() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(1, "", "permission denied"));

        UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
                unix,
                "unix.app.os",
                "Unix app OS",
                EnvironmentCheckSeverity.ERROR,
                "app",
                new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals("Failed to read Unix OS release. Command exit code: 1", result.message());
        assertEquals("permission denied", result.details().get("stderr"));
    }

    @Test
    void shouldFailWhenFacadeThrowsException() {
        RuntimeException error = new RuntimeException("Connection refused.");
        RecordingUnixFacade unix = new RecordingUnixFacade(error);

        UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
                unix,
                "unix.app.os",
                "Unix app OS",
                EnvironmentCheckSeverity.ERROR,
                "app",
                new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(
                "Unix operating system environment check failed with exception: Connection refused.",
                result.message()
        );
        assertEquals(RuntimeException.class.getName(), result.details().get("exceptionType"));
        assertEquals(error, result.error());
    }

    @Test
    void shouldUseExplicitUserAlias() {
        RecordingUnixFacade unix = new RecordingUnixFacade(osReleaseResult("rhel", "9.5"));

        UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
                unix,
                "unix.app.os",
                "Unix app OS",
                EnvironmentCheckSeverity.ERROR,
                "app",
                "oracle",
                new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals("app", unix.lastHostAlias());
        assertEquals("oracle", unix.lastUserAlias());
        assertEquals("cat /etc/os-release", unix.lastCommandRequest().command());
        assertEquals("oracle", result.details().get("userAlias"));
    }

    @Test
    void shouldFailWhenUnixFacadeIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixOperatingSystemEnvironmentCheck(
                        null,
                        "unix.app.os",
                        "Unix app OS",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                        Duration.ofSeconds(5)
                )
        );

        assertEquals("Unix facade must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenParserIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixOperatingSystemEnvironmentCheck(
                        new RecordingUnixFacade(osReleaseResult("rhel", "9.5")),
                        null,
                        "unix.app.os",
                        "Unix app OS",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        null,
                        new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                        Duration.ofSeconds(5)
                )
        );

        assertEquals("OS release parser must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenIdIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixOperatingSystemEnvironmentCheck(
                        new RecordingUnixFacade(osReleaseResult("rhel", "9.5")),
                        " ",
                        "Unix app OS",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                        Duration.ofSeconds(5)
                )
        );

        assertEquals("Environment check id must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixOperatingSystemEnvironmentCheck(
                        new RecordingUnixFacade(osReleaseResult("rhel", "9.5")),
                        "unix.app.os",
                        " ",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                        Duration.ofSeconds(5)
                )
        );

        assertEquals("Environment check name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenSeverityIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixOperatingSystemEnvironmentCheck(
                        new RecordingUnixFacade(osReleaseResult("rhel", "9.5")),
                        "unix.app.os",
                        "Unix app OS",
                        null,
                        "app",
                        new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                        Duration.ofSeconds(5)
                )
        );

        assertEquals("Environment check severity must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAliasIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixOperatingSystemEnvironmentCheck(
                        new RecordingUnixFacade(osReleaseResult("rhel", "9.5")),
                        "unix.app.os",
                        "Unix app OS",
                        EnvironmentCheckSeverity.ERROR,
                        " ",
                        new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                        Duration.ofSeconds(5)
                )
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldExecuteRawOsReleaseCommandWhenShellProfileIsMissing() {
        RecordingUnixFacade unix = new RecordingUnixFacade(osReleaseResult("rhel", "9.5"));

        UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
                unix,
                "unix.app.os",
                "Unix app OS",
                EnvironmentCheckSeverity.ERROR,
                "app",
                null,
                null,
                new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals("cat /etc/os-release", unix.lastCommandRequest().command());
        assertEquals("", result.details().get("shellProfile"));
        assertEquals("cat /etc/os-release", result.details().get("command"));
        assertEquals("cat /etc/os-release", result.details().get("rawCommand"));
    }

    @Test
    void shouldExecuteOsReleaseCommandThroughShellProfileWhenProvided() {
        RecordingUnixFacade unix = new RecordingUnixFacade(osReleaseResult("rhel", "9.5"));

        UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
                unix,
                "unix.db.os",
                "Unix db OS",
                EnvironmentCheckSeverity.ERROR,
                "db",
                "oracle",
                "/home/oracle/.profile",
                new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals("db", unix.lastHostAlias());
        assertEquals("oracle", unix.lastUserAlias());
        assertEquals(
                ". '/home/oracle/.profile' >/dev/null 2>&1 && cat /etc/os-release",
                unix.lastCommandRequest().command()
        );
        assertEquals("/home/oracle/.profile", result.details().get("shellProfile"));
        assertEquals(
                ". '/home/oracle/.profile' >/dev/null 2>&1 && cat /etc/os-release",
                result.details().get("command")
        );
        assertEquals("cat /etc/os-release", result.details().get("rawCommand"));
    }

    /**
     * Creates command result with minimal {@code /etc/os-release} content.
     *
     * @param id OS id.
     * @param versionId OS version id.
     * @return command result.
     */
    private static CommandResult osReleaseResult(
            String id,
            String versionId
    ) {
        return new CommandResult(
                0,
                """
                        NAME="Test Linux"
                        ID="%s"
                        VERSION_ID="%s"
                        PRETTY_NAME="Test Linux %s"
                        """.formatted(id, versionId, versionId),
                ""
        );
    }

    /**
     * Recording Unix facade used to verify check behavior without real SSH.
     */
    private static class RecordingUnixFacade extends UnixFacade {

        private final CommandResult result;
        private final RuntimeException exception;

        private String lastHostAlias;
        private String lastUserAlias;
        private UnixCommandRequest lastCommandRequest;

        /**
         * Creates a facade that returns a fixed command result.
         *
         * @param result command result.
         */
        private RecordingUnixFacade(CommandResult result) {
            super(new NoOpUnixSessionProvider(), new NoOpUnixHostSessionRequestFactory());

            this.result = result;
            this.exception = null;
        }

        /**
         * Creates a facade that throws a fixed exception.
         *
         * @param exception exception to throw.
         */
        private RecordingUnixFacade(RuntimeException exception) {
            super(new NoOpUnixSessionProvider(), new NoOpUnixHostSessionRequestFactory());

            this.result = null;
            this.exception = exception;
        }

        /**
         * Records default-user command execution.
         *
         * @param hostAlias host alias.
         * @param request command request.
         * @return configured command result.
         */
        @Override
        public CommandResult execute(String hostAlias, UnixCommandRequest request) {
            return execute(hostAlias, null, request);
        }

        /**
         * Records explicit-user command execution.
         *
         * @param hostAlias host alias.
         * @param userAlias user alias.
         * @param request command request.
         * @return configured command result.
         */
        @Override
        public CommandResult execute(
                String hostAlias,
                String userAlias,
                UnixCommandRequest request
        ) {
            lastHostAlias = hostAlias;
            lastUserAlias = userAlias;
            lastCommandRequest = request;

            if (exception != null) {
                throw exception;
            }

            return result;
        }

        /**
         * Returns the last host alias used by the check.
         *
         * @return last host alias.
         */
        private String lastHostAlias() {
            return lastHostAlias;
        }

        /**
         * Returns the last user alias used by the check.
         *
         * @return last user alias.
         */
        private String lastUserAlias() {
            return lastUserAlias;
        }

        /**
         * Returns the last command request used by the check.
         *
         * @return last command request.
         */
        private UnixCommandRequest lastCommandRequest() {
            return lastCommandRequest;
        }
    }

    /**
     * No-op provider required only to construct a test facade.
     */
    private static class NoOpUnixSessionProvider implements UnixSessionProvider {

        /**
         * Opens no real session.
         *
         * @param request Unix host session request.
         * @return no-op session.
         */
        @Override
        public UnixHostSession open(UnixHostSessionRequest request) {
            throw new UnsupportedOperationException("No-op provider should not be used by this test.");
        }

        /**
         * Closes no resources.
         */
        @Override
        public void close() {
            // Nothing to close.
        }
    }

    /**
     * No-op request factory required only to construct a test facade.
     */
    private static class NoOpUnixHostSessionRequestFactory extends UnixHostSessionRequestFactory {

        /**
         * Creates a no-op request factory.
         */
        private NoOpUnixHostSessionRequestFactory() {
            super(UnixTestTargets.emptyResolvedTarget());
        }
    }
}