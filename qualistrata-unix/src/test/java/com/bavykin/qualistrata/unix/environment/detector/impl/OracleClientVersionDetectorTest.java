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
 * Unit tests for {@link OracleClientVersionDetector}.
 */
class OracleClientVersionDetectorTest {

    @Test
    void shouldDetectMatchingOracleClientVersionFromStdout() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                0,
                """
                        SQL*Plus: Release 19.0.0.0.0 - Production
                        Version 19.30.0.0.0
                        """,
                ""
        ));

        UnixComponentDetectionResult result = new OracleClientVersionDetector().detect(
                unix,
                "db",
                null,
                new UnixComponentDetectionRequest("oracle-client", "19.30.0.0.0", null, Map.of()),
                Duration.ofSeconds(5)
        );

        assertTrue(result.matched());
        assertEquals("oracle-client", result.detector());
        assertEquals("oracle-client", result.componentName());
        assertEquals("19.30.0.0.0", result.actualVersion());
        assertEquals("sqlplus -versionId", unix.lastCommandRequest().command());
    }

    @Test
    void shouldDetectMatchingOracleClientVersionFromStderr() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                0,
                "",
                """
                        SQL*Plus: Release 19.0.0.0.0 - Production
                        Version 19.30.0.0.0
                        """
        ));

        UnixComponentDetectionResult result = new OracleClientVersionDetector().detect(
                unix,
                "db",
                null,
                new UnixComponentDetectionRequest("oracle-client", "19.30.0.0.0", null, Map.of()),
                Duration.ofSeconds(5)
        );

        assertTrue(result.matched());
        assertEquals("19.30.0.0.0", result.actualVersion());
    }

    @Test
    void shouldDetectMismatchingOracleClientVersion() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                0,
                "Version 19.29.0.0.0",
                ""
        ));

        UnixComponentDetectionResult result = new OracleClientVersionDetector().detect(
                unix,
                "db",
                null,
                new UnixComponentDetectionRequest("oracle-client", "19.30.0.0.0", null, Map.of()),
                Duration.ofSeconds(5)
        );

        assertFalse(result.matched());
        assertEquals("19.29.0.0.0", result.actualVersion());
        assertEquals(
                "Unix component version mismatch. Actual: 19.29.0.0.0, expected: 19.30.0.0.0",
                result.message()
        );
    }

    @Test
    void shouldExecuteCommandThroughShellProfileWhenProvided() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                0,
                "Version 19.30.0.0.0",
                ""
        ));

        UnixComponentDetectionResult result = new OracleClientVersionDetector().detect(
                unix,
                "db",
                null,
                new UnixComponentDetectionRequest(
                        "oracle-client",
                        "19.30.0.0.0",
                        "/home/oracle/.profile",
                        Map.of()
                ),
                Duration.ofSeconds(5)
        );

        assertTrue(result.matched());
        assertEquals(
                ". '/home/oracle/.profile' >/dev/null 2>&1 && sqlplus -versionId",
                unix.lastCommandRequest().command()
        );
    }

    @Test
    void shouldExecuteCommandThroughShellProfileWithExplicitUserAlias() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                0,
                "Version 19.30.0.0.0",
                ""
        ));

        UnixComponentDetectionResult result = new OracleClientVersionDetector().detect(
                unix,
                "db",
                "oracle",
                new UnixComponentDetectionRequest(
                        "oracle-client",
                        "19.30.0.0.0",
                        "/home/oracle/.profile",
                        Map.of()
                ),
                Duration.ofSeconds(5)
        );

        assertTrue(result.matched());
        assertEquals("db", unix.lastHostAlias());
        assertEquals("oracle", unix.lastUserAlias());
        assertEquals(
                ". '/home/oracle/.profile' >/dev/null 2>&1 && sqlplus -versionId",
                unix.lastCommandRequest().command()
        );
    }

    @Test
    void shouldReturnFailedResultWhenCommandFails() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                127,
                "",
                "sqlplus: command not found"
        ));

        UnixComponentDetectionResult result = new OracleClientVersionDetector().detect(
                unix,
                "db",
                null,
                new UnixComponentDetectionRequest("oracle-client", "19.30.0.0.0", null, Map.of()),
                Duration.ofSeconds(5)
        );

        assertFalse(result.matched());
        assertEquals("", result.actualVersion());
        assertEquals(
                "Unix component detector command failed with exit code: 127",
                result.message()
        );
    }

    @Test
    void shouldReturnFailedResultWhenVersionCannotBeParsed() {
        RecordingUnixFacade unix = new RecordingUnixFacade(new CommandResult(
                0,
                "SQL*Plus is installed but version output is unexpected",
                ""
        ));

        UnixComponentDetectionResult result = new OracleClientVersionDetector().detect(
                unix,
                "db",
                null,
                new UnixComponentDetectionRequest("oracle-client", "19.30.0.0.0", null, Map.of()),
                Duration.ofSeconds(5)
        );

        assertFalse(result.matched());
        assertEquals("", result.actualVersion());
        assertEquals(
                "Unix component version mismatch. Actual: , expected: 19.30.0.0.0",
                result.message()
        );
    }

    /**
     * Recording Unix facade used to verify detector behavior without real SSH.
     */
    private static class RecordingUnixFacade extends UnixFacade {

        private final CommandResult result;
        private String lastHostAlias;
        private String lastUserAlias;
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
            return execute(hostAlias, null, request);
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
            lastHostAlias = hostAlias;
            lastUserAlias = userAlias;
            lastCommandRequest = request;

            return result;
        }

        /**
         * Returns last host alias.
         *
         * @return last host alias.
         */
        private String lastHostAlias() {
            return lastHostAlias;
        }

        /**
         * Returns last user alias.
         *
         * @return last user alias.
         */
        private String lastUserAlias() {
            return lastUserAlias;
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