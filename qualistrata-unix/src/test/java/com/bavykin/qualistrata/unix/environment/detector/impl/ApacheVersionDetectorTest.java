package com.bavykin.qualistrata.unix.environment.detector.impl;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionRequest;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionResult;
import com.bavykin.qualistrata.unix.facade.UnixFacade;
import com.bavykin.qualistrata.unix.session.UnixHostSession;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionProvider;
import com.bavykin.qualistrata.unix.session.request.UnixHostSessionRequestFactory;
import com.bavykin.qualistrata.unix.testsupport.UnixTestTargets;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ApacheVersionDetector}.
 */
class ApacheVersionDetectorTest {

    @Test
    void shouldDetectMatchingApacheVersionFromStdout() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                0,
                "Server version: Apache/2.4.62 (Red Hat Enterprise Linux)",
                ""
        ));

        UnixComponentDetectionResult result = new ApacheVersionDetector().detect(
                unix,
                "app",
                null,
                new UnixComponentDetectionRequest("apache", "2.4.62", null, Map.of()),
                Duration.ofSeconds(5)
        );

        assertTrue(result.matched());
        assertEquals("apache", result.detector());
        assertEquals("apache", result.componentName());
        assertEquals("2.4.62", result.actualVersion());
        assertEquals("httpd -v || apache2 -v", unix.lastCommandRequest().command());
    }

    @Test
    void shouldDetectMatchingApacheVersionFromStderr() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                0,
                "",
                "Server version: Apache/2.4.62 (Debian)"
        ));

        UnixComponentDetectionResult result = new ApacheVersionDetector().detect(
                unix,
                "app",
                null,
                new UnixComponentDetectionRequest("apache", "2.4.62", null, Map.of()),
                Duration.ofSeconds(5)
        );

        assertTrue(result.matched());
        assertEquals("2.4.62", result.actualVersion());
    }

    @Test
    void shouldDetectMismatchingApacheVersion() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                0,
                "Server version: Apache/2.4.57",
                ""
        ));

        UnixComponentDetectionResult result = new ApacheVersionDetector().detect(
                unix,
                "app",
                null,
                new UnixComponentDetectionRequest("apache", "2.4.62", null, Map.of()),
                Duration.ofSeconds(5)
        );

        assertFalse(result.matched());
        assertEquals("2.4.57", result.actualVersion());
        assertEquals(
                "Unix component version mismatch. Actual: 2.4.57, expected: 2.4.62",
                result.message()
        );
    }

    @Test
    void shouldSourceProfileBeforeCommand() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                0,
                "Server version: Apache/2.4.62",
                ""
        ));

        new ApacheVersionDetector().detect(
                unix,
                "app",
                null,
                new UnixComponentDetectionRequest(
                        "apache",
                        "2.4.62",
                        "/opt/app/.profile",
                        Map.of()
                ),
                Duration.ofSeconds(5)
        );

        assertEquals(
                ". '/opt/app/.profile' >/dev/null 2>&1 && httpd -v || apache2 -v",
                unix.lastCommandRequest().command()
        );
    }

    @Test
    void shouldReturnFailedResultWhenCommandFails() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                127,
                "",
                "httpd: command not found"
        ));

        UnixComponentDetectionResult result = new ApacheVersionDetector().detect(
                unix,
                "app",
                null,
                new UnixComponentDetectionRequest("apache", "2.4.62", null, Map.of()),
                Duration.ofSeconds(5)
        );

        assertFalse(result.matched());
        assertEquals(
                "Unix component detector command failed with exit code: 127",
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
            super(UnixTestTargets.emptyResolvedTarget());
        }
    }
}