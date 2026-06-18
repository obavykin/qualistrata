package com.bavykin.qualistrata.unix.environment.detector.impl;

import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionRequest;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionResult;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectorException;
import com.bavykin.qualistrata.unix.facade.UnixFacade;
import com.bavykin.qualistrata.unix.session.UnixHostSession;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionProvider;
import com.bavykin.qualistrata.unix.session.request.UnixHostSessionRequestFactory;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link PythonPackageVersionDetector}.
 */
class PythonPackageVersionDetectorTest {

    @Test
    void shouldDetectMatchingPythonPackageVersion() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "6.0.4", ""));

        UnixComponentDetectionResult result = new PythonPackageVersionDetector().detect(
                unix,
                "app",
                null,
                new UnixComponentDetectionRequest(
                        "python-package",
                        "6.0.4",
                        null,
                        Map.of("package", "Django")
                ),
                Duration.ofSeconds(5)
        );

        assertTrue(result.matched());
        assertEquals("python-package", result.detector());
        assertEquals("Django", result.componentName());
        assertEquals("6.0.4", result.actualVersion());
        assertEquals(
                "python3 -c \"import importlib.metadata as m; print(m.version('Django'))\"",
                unix.lastCommandRequest().command()
        );
    }

    @Test
    void shouldDetectMismatchingPythonPackageVersion() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "5.2.1", ""));

        UnixComponentDetectionResult result = new PythonPackageVersionDetector().detect(
                unix,
                "app",
                null,
                new UnixComponentDetectionRequest(
                        "python-package",
                        "6.0.4",
                        null,
                        Map.of("package", "Django")
                ),
                Duration.ofSeconds(5)
        );

        assertFalse(result.matched());
        assertEquals("5.2.1", result.actualVersion());
        assertEquals(
                "Unix component version mismatch. Actual: 5.2.1, expected: 6.0.4",
                result.message()
        );
    }

    @Test
    void shouldSourceProfileBeforeCommand() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "6.0.4", ""));

        new PythonPackageVersionDetector().detect(
                unix,
                "app",
                null,
                new UnixComponentDetectionRequest(
                        "python-package",
                        "6.0.4",
                        "/opt/app/.profile",
                        Map.of("package", "Django")
                ),
                Duration.ofSeconds(5)
        );

        assertEquals(
                ". '/opt/app/.profile' >/dev/null 2>&1 && python3 -c \"import importlib.metadata as m; print(m.version('Django'))\"",
                unix.lastCommandRequest().command()
        );
    }

    @Test
    void shouldEscapePackageNameInPythonStringLiteral() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(0, "1.0.0", ""));

        new PythonPackageVersionDetector().detect(
                unix,
                "app",
                null,
                new UnixComponentDetectionRequest(
                        "python-package",
                        "1.0.0",
                        null,
                        Map.of("package", "demo'package")
                ),
                Duration.ofSeconds(5)
        );

        assertEquals(
                "python3 -c \"import importlib.metadata as m; print(m.version('demo\\'package'))\"",
                unix.lastCommandRequest().command()
        );
    }

    @Test
    void shouldFailWhenPackagePropertyIsMissing() {
        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> new PythonPackageVersionDetector().detect(
                        new RecordingUnixFacade(new CommandResult(0, "6.0.4", "")),
                        "app",
                        null,
                        new UnixComponentDetectionRequest(
                                "python-package",
                                "6.0.4",
                                null,
                                Map.of()
                        ),
                        Duration.ofSeconds(5)
                )
        );

        assertEquals(
                "Required Unix component detector property is missing or blank: package",
                exception.getMessage()
        );
    }

    @Test
    void shouldReturnFailedResultWhenPackageIsMissingOnHost() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                1,
                "",
                "importlib.metadata.PackageNotFoundError: No package metadata was found for Django"
        ));

        UnixComponentDetectionResult result = new PythonPackageVersionDetector().detect(
                unix,
                "app",
                null,
                new UnixComponentDetectionRequest(
                        "python-package",
                        "6.0.4",
                        null,
                        Map.of("package", "Django")
                ),
                Duration.ofSeconds(5)
        );

        assertFalse(result.matched());
        assertEquals("", result.actualVersion());
        assertEquals(
                "Unix component detector command failed with exit code: 1",
                result.message()
        );
    }

    /**
     * Recording Unix facade used to verify detector behavior without real SSH.
     */
    private static class RecordingUnixFacade extends UnixFacade {

        private final CommandResult result;
        private UnixCommandRequest lastCommandRequest;

        /**
         * Creates a recording facade.
         *
         * @param result command result to return.
         */
        private RecordingUnixFacade(CommandResult result) {
            super(new NoOpUnixSessionProvider(), new NoOpUnixHostSessionRequestFactory());
            this.result = result;
        }

        /**
         * Records command execution.
         *
         * @param hostAlias host alias.
         * @param request command request.
         * @return configured command result.
         */
        @Override
        public CommandResult execute(String hostAlias, UnixCommandRequest request) {
            lastCommandRequest = request;
            return result;
        }

        /**
         * Records command execution with explicit user alias.
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
            lastCommandRequest = request;
            return result;
        }

        /**
         * Returns last command request.
         *
         * @return last command request.
         */
        private UnixCommandRequest lastCommandRequest() {
            return lastCommandRequest;
        }
    }

    /**
     * No-op Unix session provider required only for test facade construction.
     */
    private static class NoOpUnixSessionProvider implements UnixSessionProvider {

        /**
         * Opens no real session.
         *
         * @param request session request.
         * @return no session.
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
     * No-op request factory required only for test facade construction.
     */
    private static class NoOpUnixHostSessionRequestFactory extends UnixHostSessionRequestFactory {

        /**
         * Creates a no-op request factory.
         */
        private NoOpUnixHostSessionRequestFactory() {
            super(new TargetConfig("test", Map.of(), null, null, null, null));
        }
    }
}