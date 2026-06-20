package com.bavykin.qualistrata.unix.environment.detector;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSeverity;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckStatus;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.facade.UnixFacade;
import com.bavykin.qualistrata.unix.session.UnixHostSession;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionProvider;
import com.bavykin.qualistrata.unix.session.request.UnixHostSessionRequestFactory;
import com.bavykin.qualistrata.unix.testsupport.UnixTestTargets;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DetectedUnixComponentEnvironmentCheck}.
 */
class DetectedUnixComponentEnvironmentCheckTest {

    @Test
    void shouldReturnPassedResultWhenDetectedVersionMatches() {
        UnixComponentDetectionRequest request = new UnixComponentDetectionRequest(
                "stub",
                "1.0.0",
                "/opt/app/.profile",
                Map.of()
        );

        DetectedUnixComponentEnvironmentCheck check = new DetectedUnixComponentEnvironmentCheck(
                "app.stub.version",
                "App stub version",
                EnvironmentCheckSeverity.ERROR,
                new RecordingUnixFacade(),
                "app",
                null,
                request,
                new StubRegistry(new StubDetector(new UnixComponentDetectionResult(
                        "stub",
                        "stub",
                        "1.0.0",
                        "1.0.0",
                        "stub --version",
                        new CommandResult(0, "1.0.0", ""),
                        true,
                        "Unix component version matches expected baseline."
                ))),
                        Duration.ofSeconds(5)
                );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.PASSED, result.status());
        assertEquals(EnvironmentCheckSeverity.INFO, result.severity());
        assertEquals("Unix component version matches expected baseline.", result.message());
        assertEquals("app", result.details().get("hostAlias"));
        assertEquals("/opt/app/.profile", result.details().get("shellProfile"));
        assertEquals("stub", result.details().get("detector"));
        assertEquals("1.0.0", result.details().get("actualVersion"));
        assertNotNull(result.duration());
    }

    @Test
    void shouldReturnFailedResultWhenDetectedVersionDoesNotMatch() {
        UnixComponentDetectionRequest request = new UnixComponentDetectionRequest(
                "stub",
                "1.0.0",
                null,
                Map.of()
        );

        DetectedUnixComponentEnvironmentCheck check = new DetectedUnixComponentEnvironmentCheck(
                "app.stub.version",
                "App stub version",
                EnvironmentCheckSeverity.ERROR,
                new RecordingUnixFacade(),
                "app",
                null,
                request,
                new StubRegistry(new StubDetector(new UnixComponentDetectionResult(
                        "stub",
                        "stub",
                        "1.0.0",
                        "2.0.0",
                        "stub --version",
                        new CommandResult(0, "2.0.0", ""),
                        false,
                        "Unix component version mismatch. Actual: 2.0.0, expected: 1.0.0"
                ))),
                        Duration.ofSeconds(5)
                );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(EnvironmentCheckSeverity.ERROR, result.severity());
        assertEquals(
                "Unix component version mismatch. Actual: 2.0.0, expected: 1.0.0",
                result.message()
        );
        assertEquals("false", result.details().get("matched"));
    }

    @Test
    void shouldReturnFailedResultWhenDetectorThrowsException() {
        UnixComponentDetectionRequest request = new UnixComponentDetectionRequest(
                "stub",
                "1.0.0",
                null,
                Map.of()
        );

        RuntimeException error = new RuntimeException("Detector failed.");

        DetectedUnixComponentEnvironmentCheck check = new DetectedUnixComponentEnvironmentCheck(
                "app.stub.version",
                "App stub version",
                EnvironmentCheckSeverity.ERROR,
                new RecordingUnixFacade(),
                "app",
                null,
                request,
                new StubRegistry(new ThrowingDetector(error)),
                Duration.ofSeconds(5)
        );

        EnvironmentCheckResult result = check.check();

        assertEquals(EnvironmentCheckStatus.FAILED, result.status());
        assertEquals(EnvironmentCheckSeverity.ERROR, result.severity());
        assertEquals(
                "Unix component detector check failed with exception: Detector failed.",
                result.message()
        );
        assertEquals(RuntimeException.class.getName(), result.details().get("exceptionType"));
        assertEquals(error, result.error());
    }

    @Test
    void shouldPassExplicitUserAliasToDetector() {
        UnixComponentDetectionRequest request = new UnixComponentDetectionRequest(
                "stub",
                "1.0.0",
                null,
                Map.of()
        );

        RecordingDetector detector = new RecordingDetector();

        DetectedUnixComponentEnvironmentCheck check = new DetectedUnixComponentEnvironmentCheck(
                "app.stub.version",
                "App stub version",
                EnvironmentCheckSeverity.ERROR,
                new RecordingUnixFacade(),
                "app",
                "oracle",
                request,
                new StubRegistry(detector),
                Duration.ofSeconds(5)
        );

        check.check();

        assertEquals("app", detector.hostAlias());
        assertEquals("oracle", detector.userAlias());
    }

    @Test
    void shouldFailWhenIdIsBlank() {
        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> new DetectedUnixComponentEnvironmentCheck(
                        " ",
                        "App stub version",
                        EnvironmentCheckSeverity.ERROR,
                        new RecordingUnixFacade(),
                        "app",
                        new UnixComponentDetectionRequest("stub", "1.0.0", null, Map.of())
                )
        );

        assertEquals("Environment check id must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> new DetectedUnixComponentEnvironmentCheck(
                        "app.stub.version",
                        " ",
                        EnvironmentCheckSeverity.ERROR,
                        new RecordingUnixFacade(),
                        "app",
                        new UnixComponentDetectionRequest("stub", "1.0.0", null, Map.of())
                )
        );

        assertEquals("Environment check name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenSeverityIsNull() {
        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> new DetectedUnixComponentEnvironmentCheck(
                        "app.stub.version",
                        "App stub version",
                        null,
                        new RecordingUnixFacade(),
                        "app",
                        new UnixComponentDetectionRequest("stub", "1.0.0", null, Map.of())
                )
        );

        assertEquals("Environment check severity must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenUnixFacadeIsNull() {
        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> new DetectedUnixComponentEnvironmentCheck(
                        "app.stub.version",
                        "App stub version",
                        EnvironmentCheckSeverity.ERROR,
                        null,
                        "app",
                        new UnixComponentDetectionRequest("stub", "1.0.0", null, Map.of())
                )
        );

        assertEquals("Unix facade must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAliasIsBlank() {
        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> new DetectedUnixComponentEnvironmentCheck(
                        "app.stub.version",
                        "App stub version",
                        EnvironmentCheckSeverity.ERROR,
                        new RecordingUnixFacade(),
                        " ",
                        new UnixComponentDetectionRequest("stub", "1.0.0", null, Map.of())
                )
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenRequestIsNull() {
        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> new DetectedUnixComponentEnvironmentCheck(
                        "app.stub.version",
                        "App stub version",
                        EnvironmentCheckSeverity.ERROR,
                        new RecordingUnixFacade(),
                        "app",
                        null
                )
        );

        assertEquals("Unix component detection request must not be null.", exception.getMessage());
    }

    /**
     * Registry that always returns the configured stub detector.
     */
    private static class StubRegistry implements UnixComponentDetectorRegistry {

        private final UnixComponentDetector detector;

        /**
         * Creates a stub registry.
         *
         * @param detector detector to return.
         */
        private StubRegistry(UnixComponentDetector detector) {
            this.detector = detector;
        }

        /**
         * Returns configured detector.
         *
         * @param detectorId detector id.
         * @return configured detector.
         */
        @Override
        public UnixComponentDetector detector(String detectorId) {
            return detector;
        }

        /**
         * Returns configured detector collection.
         *
         * @return detectors.
         */
        @Override
        public Collection<UnixComponentDetector> detectors() {
            return List.of(detector);
        }
    }

    /**
     * Detector that returns a predefined result.
     */
    private static class StubDetector implements UnixComponentDetector {

        private final UnixComponentDetectionResult result;

        /**
         * Creates a stub detector.
         *
         * @param result detection result.
         */
        private StubDetector(UnixComponentDetectionResult result) {
            this.result = result;
        }

        /**
         * Returns detector id.
         *
         * @return detector id.
         */
        @Override
        public String id() {
            return "stub";
        }

        /**
         * Returns predefined result.
         *
         * @param unixFacade Unix facade.
         * @param hostAlias host alias.
         * @param userAlias user alias.
         * @param request detection request.
         * @param timeout timeout.
         * @return predefined result.
         */
        @Override
        public UnixComponentDetectionResult detect(
                UnixFacade unixFacade,
                String hostAlias,
                String userAlias,
                UnixComponentDetectionRequest request,
                Duration timeout
        ) {
            return result;
        }
    }

    /**
     * Detector that records host and user aliases.
     */
    private static class RecordingDetector implements UnixComponentDetector {

        private String hostAlias;
        private String userAlias;

        /**
         * Returns detector id.
         *
         * @return detector id.
         */
        @Override
        public String id() {
            return "stub";
        }

        /**
         * Records aliases and returns passed detection result.
         *
         * @param unixFacade Unix facade.
         * @param hostAlias host alias.
         * @param userAlias user alias.
         * @param request detection request.
         * @param timeout timeout.
         * @return detection result.
         */
        @Override
        public UnixComponentDetectionResult detect(
                UnixFacade unixFacade,
                String hostAlias,
                String userAlias,
                UnixComponentDetectionRequest request,
                Duration timeout
        ) {
            this.hostAlias = hostAlias;
            this.userAlias = userAlias;

            return new UnixComponentDetectionResult(
                    "stub",
                    "stub",
                    request.expectedVersion(),
                    request.expectedVersion(),
                    "stub --version",
                    new CommandResult(0, request.expectedVersion(), ""),
                    true,
                    "Unix component version matches expected baseline."
            );
        }

        /**
         * Returns recorded host alias.
         *
         * @return host alias.
         */
        private String hostAlias() {
            return hostAlias;
        }

        /**
         * Returns recorded user alias.
         *
         * @return user alias.
         */
        private String userAlias() {
            return userAlias;
        }
    }

    /**
     * Detector that always throws the configured exception.
     */
    private static class ThrowingDetector implements UnixComponentDetector {

        private final RuntimeException exception;

        /**
         * Creates a throwing detector.
         *
         * @param exception exception to throw.
         */
        private ThrowingDetector(RuntimeException exception) {
            this.exception = exception;
        }

        /**
         * Returns detector id.
         *
         * @return detector id.
         */
        @Override
        public String id() {
            return "stub";
        }

        /**
         * Throws configured exception.
         *
         * @param unixFacade Unix facade.
         * @param hostAlias host alias.
         * @param userAlias user alias.
         * @param request detection request.
         * @param timeout timeout.
         * @return never returns.
         */
        @Override
        public UnixComponentDetectionResult detect(
                UnixFacade unixFacade,
                String hostAlias,
                String userAlias,
                UnixComponentDetectionRequest request,
                Duration timeout
        ) {
            throw exception;
        }
    }

    /**
     * Recording Unix facade required only for check construction.
     */
    private static class RecordingUnixFacade extends UnixFacade {

        /**
         * Creates a recording facade.
         */
        private RecordingUnixFacade() {
            super(new NoOpUnixSessionProvider(), new NoOpUnixHostSessionRequestFactory());
        }

        /**
         * Records command execution.
         *
         * @param hostAlias host alias.
         * @param request command request.
         * @return dummy command result.
         */
        @Override
        public CommandResult execute(String hostAlias, UnixCommandRequest request) {
            return new CommandResult(0, "", "");
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
            super(UnixTestTargets.emptyResolvedTarget());
        }
    }
}