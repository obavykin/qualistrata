package com.bavykin.qualistrata.unix.environment;

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
 * Unit tests for {@link UnixCommandEnvironmentCheck}.
 */
class UnixCommandEnvironmentCheckTest {

    @Test
    void shouldReturnPassedResultWhenCommandExitCodeIsZero() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "ok", ""));

        UnixCommandEnvironmentCheck check = new UnixCommandEnvironmentCheck(
                unix,
                "unix.app.hostname",
                "Unix app hostname",
                EnvironmentCheckSeverity.ERROR,
                "app",
                "hostname",
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals(EnvironmentCheckSeverity.INFO, result.severity());
        assertEquals("unix.app.hostname", result.id());
        assertEquals("Unix app hostname", result.name());
        assertEquals("Unix command environment check passed.", result.message());
        assertEquals("app", result.details().get("hostAlias"));
        assertEquals("", result.details().get("userAlias"));
        assertEquals("", result.details().get("shellProfile"));
        assertEquals("hostname", result.details().get("command"));
        assertEquals("hostname", result.details().get("rawCommand"));
        assertEquals("0", result.details().get("exitCode"));
        assertEquals("ok", result.details().get("stdout"));
        assertEquals("", result.details().get("stderr"));
        assertNotNull(result.duration());
    }

    @Test
    void shouldReturnFailedResultWhenCommandExitCodeIsNonZero() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(2, "", "not found"));

        UnixCommandEnvironmentCheck check = new UnixCommandEnvironmentCheck(
                unix,
                "unix.app.path",
                "Unix app path",
                EnvironmentCheckSeverity.ERROR,
                "app",
                "test -d /missing",
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(EnvironmentCheckSeverity.ERROR, result.severity());
        assertEquals("Unix command environment check failed with exit code: 2", result.message());
        assertEquals("2", result.details().get("exitCode"));
        assertEquals("not found", result.details().get("stderr"));
    }

    @Test
    void shouldUseExplicitUserAlias() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "oracle", ""));

        UnixCommandEnvironmentCheck check = new UnixCommandEnvironmentCheck(
                unix,
                "unix.app.whoami",
                "Unix app whoami",
                EnvironmentCheckSeverity.ERROR,
                "app",
                "oracle",
                "whoami",
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals("app", unix.lastHostAlias());
        assertEquals("oracle", unix.lastUserAlias());
        assertEquals("whoami", unix.lastCommandRequest().command());
        assertEquals("oracle", result.details().get("userAlias"));
        assertEquals("", result.details().get("shellProfile"));
    }

    @Test
    void shouldExecuteRawCommandWhenShellProfileIsMissing() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "ok", ""));

        UnixCommandEnvironmentCheck check = new UnixCommandEnvironmentCheck(
                unix,
                "unix.app.hostname",
                "Unix app hostname",
                EnvironmentCheckSeverity.ERROR,
                "app",
                null,
                null,
                new UnixCommandRequest(
                        "hostname",
                        Duration.ofSeconds(5)
                )
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals("hostname", unix.lastCommandRequest().command());
        assertEquals("", result.details().get("shellProfile"));
        assertEquals("hostname", result.details().get("command"));
        assertEquals("hostname", result.details().get("rawCommand"));
    }

    @Test
    void shouldExecuteCommandThroughShellProfileWhenProvided() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "ok", ""));

        UnixCommandEnvironmentCheck check = new UnixCommandEnvironmentCheck(
                unix,
                "unix.db.sqlplus",
                "Unix db sqlplus",
                EnvironmentCheckSeverity.ERROR,
                "db",
                null,
                "/home/oracle/.profile",
                new UnixCommandRequest(
                        "sqlplus -v",
                        Duration.ofSeconds(5)
                )
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals(
                ". '/home/oracle/.profile' >/dev/null 2>&1 && sqlplus -v",
                unix.lastCommandRequest().command()
        );
        assertEquals("/home/oracle/.profile", result.details().get("shellProfile"));
        assertEquals(
                ". '/home/oracle/.profile' >/dev/null 2>&1 && sqlplus -v",
                result.details().get("command")
        );
        assertEquals("sqlplus -v", result.details().get("rawCommand"));
    }

    @Test
    void shouldExecuteCommandThroughShellProfileWithExplicitUserAlias() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "oracle", ""));

        UnixCommandEnvironmentCheck check = new UnixCommandEnvironmentCheck(
                unix,
                "unix.db.whoami",
                "Unix db whoami",
                EnvironmentCheckSeverity.ERROR,
                "db",
                "oracle",
                "/home/oracle/.profile",
                new UnixCommandRequest(
                        "whoami",
                        Duration.ofSeconds(5)
                )
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals("db", unix.lastHostAlias());
        assertEquals("oracle", unix.lastUserAlias());
        assertEquals(
                ". '/home/oracle/.profile' >/dev/null 2>&1 && whoami",
                unix.lastCommandRequest().command()
        );
        assertEquals("/home/oracle/.profile", result.details().get("shellProfile"));
        assertEquals("whoami", result.details().get("rawCommand"));
    }

    @Test
    void shouldReturnFailedResultWhenFacadeThrowsException() {
        RuntimeException error = new RuntimeException("Connection refused.");
        RecordingUnixFacade unix = new RecordingUnixFacade(error);

        UnixCommandEnvironmentCheck check = new UnixCommandEnvironmentCheck(
                unix,
                "unix.app.connectivity",
                "Unix app connectivity",
                EnvironmentCheckSeverity.ERROR,
                "app",
                "hostname",
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(EnvironmentCheckSeverity.ERROR, result.severity());
        assertEquals(
                "Unix command environment check failed with exception: Connection refused.",
                result.message()
        );
        assertEquals(RuntimeException.class.getName(), result.details().get("exceptionType"));
        assertEquals("hostname", result.details().get("command"));
        assertEquals("hostname", result.details().get("rawCommand"));
        assertEquals(error, result.error());
    }

    @Test
    void shouldFailWhenUnixFacadeIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixCommandEnvironmentCheck(
                        null,
                        "unix.app.hostname",
                        "Unix app hostname",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        "hostname",
                        Duration.ofSeconds(5)
                )
        );

        assertEquals("Unix facade must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenIdIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixCommandEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "ok", "")),
                        " ",
                        "Unix app hostname",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        "hostname",
                        Duration.ofSeconds(5)
                )
        );

        assertEquals("Environment check id must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixCommandEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "ok", "")),
                        "unix.app.hostname",
                        " ",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        "hostname",
                        Duration.ofSeconds(5)
                )
        );

        assertEquals("Environment check name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenSeverityIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixCommandEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "ok", "")),
                        "unix.app.hostname",
                        "Unix app hostname",
                        null,
                        "app",
                        "hostname",
                        Duration.ofSeconds(5)
                )
        );

        assertEquals("Environment check severity must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAliasIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixCommandEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "ok", "")),
                        "unix.app.hostname",
                        "Unix app hostname",
                        EnvironmentCheckSeverity.ERROR,
                        " ",
                        "hostname",
                        Duration.ofSeconds(5)
                )
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenCommandRequestIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixCommandEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "ok", "")),
                        "unix.app.hostname",
                        "Unix app hostname",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        null,
                        null,
                        (UnixCommandRequest) null
                )
        );

        assertEquals("Unix command request must not be null.", exception.getMessage());
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