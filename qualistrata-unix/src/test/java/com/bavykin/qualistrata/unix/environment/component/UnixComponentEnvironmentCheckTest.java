package com.bavykin.qualistrata.unix.environment.component;

import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSeverity;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckStatus;
import com.bavykin.qualistrata.core.environment.matching.EnvironmentValueMatcher;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.facade.UnixFacade;
import com.bavykin.qualistrata.unix.session.UnixHostSession;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionException;
import com.bavykin.qualistrata.unix.session.UnixSessionProvider;
import com.bavykin.qualistrata.unix.session.request.UnixHostSessionRequestFactory;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link UnixComponentEnvironmentCheck}.
 */
class UnixComponentEnvironmentCheckTest {

    @Test
    void shouldPassWhenCommandOutputMatchesExpectedValue() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "Python 3.9.18", ""));

        UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
                unix,
                "unix.app.python",
                "Unix app Python",
                EnvironmentCheckSeverity.ERROR,
                "app",
                "python",
                "python3 --version",
                Duration.ofSeconds(5),
                EnvironmentValueMatcher.contains("Python 3.9")
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals(EnvironmentCheckSeverity.INFO, result.severity());
        assertEquals("Unix component baseline matches expected value.", result.message());
        assertEquals("app", result.details().get("hostAlias"));
        assertEquals("python", result.details().get("componentName"));
        assertEquals("python3 --version", result.details().get("command"));
        assertEquals("STDOUT", result.details().get("outputSource"));
        assertEquals("Python 3.9", result.details().get("expected"));
        assertEquals("Python 3.9.18", result.details().get("actual"));
        assertEquals("true", result.details().get("matched"));
        assertNotNull(result.duration());
    }

    @Test
    void shouldFailWhenCommandOutputDoesNotMatchExpectedValue() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "Python 3.8.18", ""));

        UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
                unix,
                "unix.app.python",
                "Unix app Python",
                EnvironmentCheckSeverity.ERROR,
                "app",
                "python",
                "python3 --version",
                Duration.ofSeconds(5),
                EnvironmentValueMatcher.contains("Python 3.9")
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(EnvironmentCheckSeverity.ERROR, result.severity());
        assertEquals(
                "Environment value does not contain expected text. Actual: Python 3.8.18, expected to contain: Python 3.9",
                result.message()
        );
        assertEquals("false", result.details().get("matched"));
    }

    @Test
    void shouldFailWhenCommandExitCodeIsNonZero() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(127, "", "python3: command not found"));

        UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
                unix,
                "unix.app.python",
                "Unix app Python",
                EnvironmentCheckSeverity.ERROR,
                "app",
                "python",
                "python3 --version",
                Duration.ofSeconds(5),
                EnvironmentValueMatcher.contains("Python 3.9")
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals("Unix component detection command failed with exit code: 127", result.message());
        assertEquals("python3: command not found", result.details().get("stderr"));
    }

    @Test
    void shouldUseStderrAsActualValueWhenConfigured() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "", "openjdk version \"21.0.1\""));

        UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
                unix,
                "unix.app.java",
                "Unix app Java",
                EnvironmentCheckSeverity.ERROR,
                "app",
                null,
                "java",
                "java -version",
                Duration.ofSeconds(5),
                EnvironmentValueMatcher.contains("21.0"),
                UnixComponentOutputSource.STDERR
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals("openjdk version \"21.0.1\"", result.details().get("actual"));
        assertEquals("STDERR", result.details().get("outputSource"));
    }

    @Test
    void shouldUseStdoutThenStderrFallback() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "", "Apache/2.4.62"));

        UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
                unix,
                "unix.app.httpd",
                "Unix app httpd",
                EnvironmentCheckSeverity.ERROR,
                "app",
                null,
                "httpd",
                "httpd -v",
                Duration.ofSeconds(5),
                EnvironmentValueMatcher.contains("Apache/2.4"),
                UnixComponentOutputSource.STDOUT_THEN_STDERR
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals("Apache/2.4.62", result.details().get("actual"));
    }

    @Test
    void shouldUseCombinedOutput() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "line-one", "line-two"));

        UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
                unix,
                "unix.app.combined",
                "Unix app combined",
                EnvironmentCheckSeverity.ERROR,
                "app",
                null,
                "combined",
                "component --version",
                Duration.ofSeconds(5),
                EnvironmentValueMatcher.contains("line-two"),
                UnixComponentOutputSource.COMBINED
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals("COMBINED", result.details().get("outputSource"));
    }

    @Test
    void shouldUseExplicitUserAlias() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "oracle", ""));

        UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
                unix,
                "unix.app.oracle",
                "Unix app Oracle",
                EnvironmentCheckSeverity.ERROR,
                "app",
                "oracle",
                "oracle",
                "whoami",
                Duration.ofSeconds(5),
                EnvironmentValueMatcher.exact("oracle"),
                UnixComponentOutputSource.STDOUT
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals("app", unix.lastHostAlias());
        assertEquals("oracle", unix.lastUserAlias());
        assertEquals("whoami", unix.lastCommandRequest().command());
        assertEquals("oracle", result.details().get("userAlias"));
    }

    @Test
    void shouldFailWhenFacadeThrowsException() {
        RuntimeException error = new RuntimeException("Connection refused.");
        RecordingUnixFacade unix = new RecordingUnixFacade(error);

        UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
                unix,
                "unix.app.python",
                "Unix app Python",
                EnvironmentCheckSeverity.ERROR,
                "app",
                "python",
                "python3 --version",
                Duration.ofSeconds(5),
                EnvironmentValueMatcher.contains("Python 3.9")
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(
                "Unix component environment check failed with exception: Connection refused.",
                result.message()
        );
        assertEquals(RuntimeException.class.getName(), result.details().get("exceptionType"));
        assertEquals(error, result.error());
    }

    @Test
    void shouldFailWhenUnixFacadeIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixComponentEnvironmentCheck(
                        null,
                        "unix.app.python",
                        "Unix app Python",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        "python",
                        "python3 --version",
                        Duration.ofSeconds(5),
                        EnvironmentValueMatcher.contains("Python 3.9")
                )
        );

        assertEquals("Unix facade must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenIdIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixComponentEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "Python 3.9.18", "")),
                        " ",
                        "Unix app Python",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        "python",
                        "python3 --version",
                        Duration.ofSeconds(5),
                        EnvironmentValueMatcher.contains("Python 3.9")
                )
        );

        assertEquals("Environment check id must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixComponentEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "Python 3.9.18", "")),
                        "unix.app.python",
                        " ",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        "python",
                        "python3 --version",
                        Duration.ofSeconds(5),
                        EnvironmentValueMatcher.contains("Python 3.9")
                )
        );

        assertEquals("Environment check name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenSeverityIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixComponentEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "Python 3.9.18", "")),
                        "unix.app.python",
                        "Unix app Python",
                        null,
                        "app",
                        "python",
                        "python3 --version",
                        Duration.ofSeconds(5),
                        EnvironmentValueMatcher.contains("Python 3.9")
                )
        );

        assertEquals("Environment check severity must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAliasIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixComponentEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "Python 3.9.18", "")),
                        "unix.app.python",
                        "Unix app Python",
                        EnvironmentCheckSeverity.ERROR,
                        " ",
                        "python",
                        "python3 --version",
                        Duration.ofSeconds(5),
                        EnvironmentValueMatcher.contains("Python 3.9")
                )
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenComponentNameIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixComponentEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "Python 3.9.18", "")),
                        "unix.app.python",
                        "Unix app Python",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        " ",
                        "python3 --version",
                        Duration.ofSeconds(5),
                        EnvironmentValueMatcher.contains("Python 3.9")
                )
        );

        assertEquals("Unix component name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenCommandIsBlank() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixComponentEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "Python 3.9.18", "")),
                        "unix.app.python",
                        "Unix app Python",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        "python",
                        " ",
                        Duration.ofSeconds(5),
                        EnvironmentValueMatcher.contains("Python 3.9")
                )
        );

        assertEquals("Unix component detection command must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenMatcherIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixComponentEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "Python 3.9.18", "")),
                        "unix.app.python",
                        "Unix app Python",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        "python",
                        "python3 --version",
                        Duration.ofSeconds(5),
                        null
                )
        );

        assertEquals("Environment value matcher must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenOutputSourceIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixComponentEnvironmentCheck(
                        new RecordingUnixFacade(new CommandResult(0, "Python 3.9.18", "")),
                        "unix.app.python",
                        "Unix app Python",
                        EnvironmentCheckSeverity.ERROR,
                        "app",
                        null,
                        "python",
                        "python3 --version",
                        Duration.ofSeconds(5),
                        EnvironmentValueMatcher.contains("Python 3.9"),
                        null
                )
        );

        assertEquals("Unix component output source must not be null.", exception.getMessage());
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
            super(new TargetConfig(
                    "test",
                    Map.of(),
                    null,
                    null,
                    null,
                    null
            ));
        }
    }
}