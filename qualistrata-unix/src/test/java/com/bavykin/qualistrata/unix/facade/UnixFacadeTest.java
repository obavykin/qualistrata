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
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.session.UnixHostSession;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionException;
import com.bavykin.qualistrata.unix.session.UnixSessionProvider;
import com.bavykin.qualistrata.unix.session.request.UnixHostSessionRequestFactory;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link UnixFacade}.
 */
class UnixFacadeTest {

    @Test
    void shouldFailWhenSessionProviderIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixFacade(
                        null,
                        new UnixHostSessionRequestFactory(validTargetConfig())
                )
        );

        assertEquals("Unix session provider must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenRequestFactoryIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixFacade(
                        new RecordingUnixSessionProvider(),
                        null
                )
        );

        assertEquals("Unix host session request factory must not be null.", exception.getMessage());
    }

    @Test
    void shouldOpenSessionUsingDefaultUser() {
        RecordingUnixSessionProvider provider = new RecordingUnixSessionProvider();
        UnixFacade facade = testFacade(provider);

        UnixHostSession session = facade.open("app");

        assertEquals(1, provider.openCount());
        assertEquals("app", provider.lastRequest().hostAlias());
        assertEquals("app", provider.lastRequest().resolvedUserAlias());
        assertSame(session, provider.lastSession());

        session.close();
        facade.close();
    }

    @Test
    void shouldOpenSessionUsingExplicitUserAlias() {
        RecordingUnixSessionProvider provider = new RecordingUnixSessionProvider();
        UnixFacade facade = testFacade(provider);

        UnixHostSession session = facade.open("app", "oracle");

        assertEquals(1, provider.openCount());
        assertEquals("app", provider.lastRequest().hostAlias());
        assertEquals("oracle", provider.lastRequest().resolvedUserAlias());
        assertSame(session, provider.lastSession());

        session.close();
        facade.close();
    }

    @Test
    void shouldExecuteCommandUsingDefaultUserAndCloseBorrowedSession() {
        RecordingUnixSessionProvider provider = new RecordingUnixSessionProvider();
        UnixFacade facade = testFacade(provider);

        CommandResult result = facade.execute(
                "app",
                new UnixCommandRequest(
                        "hostname",
                        Duration.ofSeconds(5)
                )
        );

        assertEquals(0, result.exitCode());
        assertEquals("app:app:hostname", result.stdout());
        assertEquals("", result.stderr());
        assertTrue(provider.lastSession().closed());

        facade.close();
    }

    @Test
    void shouldExecuteCommandUsingExplicitUserAliasAndCloseBorrowedSession() {
        RecordingUnixSessionProvider provider = new RecordingUnixSessionProvider();
        UnixFacade facade = testFacade(provider);

        CommandResult result = facade.execute(
                "app",
                "oracle",
                new UnixCommandRequest(
                        "whoami",
                        Duration.ofSeconds(5)
                )
        );

        assertEquals(0, result.exitCode());
        assertEquals("app:oracle:whoami", result.stdout());
        assertEquals("", result.stderr());
        assertTrue(provider.lastSession().closed());

        facade.close();
    }

    @Test
    void shouldCloseUnderlyingSessionProvider() {
        RecordingUnixSessionProvider provider = new RecordingUnixSessionProvider();
        UnixFacade facade = testFacade(provider);

        facade.close();

        assertTrue(provider.closed());
    }

    @Test
    void shouldExecuteCommandStringUsingDefaultUserWithoutExplicitTimeout() {
        RecordingUnixSessionProvider provider = new RecordingUnixSessionProvider();
        UnixFacade facade = testFacade(provider);

        CommandResult result = facade.execute("app", "hostname");

        assertEquals(0, result.exitCode());
        assertEquals("app:app:hostname", result.stdout());
        assertEquals("", result.stderr());
        assertTrue(provider.lastSession().closed());

        facade.close();
    }

    @Test
    void shouldExecuteCommandStringUsingDefaultUserWithExplicitTimeout() {
        RecordingUnixSessionProvider provider = new RecordingUnixSessionProvider();
        UnixFacade facade = testFacade(provider);

        CommandResult result = facade.execute(
                "app",
                "hostname",
                Duration.ofSeconds(5)
        );

        assertEquals(0, result.exitCode());
        assertEquals("app:app:hostname", result.stdout());
        assertEquals("", result.stderr());
        assertTrue(provider.lastSession().closed());

        facade.close();
    }

    @Test
    void shouldExecuteCommandStringUsingExplicitUserWithoutExplicitTimeout() {
        RecordingUnixSessionProvider provider = new RecordingUnixSessionProvider();
        UnixFacade facade = testFacade(provider);

        CommandResult result = facade.execute(
                "app",
                "oracle",
                "whoami"
        );

        assertEquals(0, result.exitCode());
        assertEquals("app:oracle:whoami", result.stdout());
        assertEquals("", result.stderr());
        assertTrue(provider.lastSession().closed());

        facade.close();
    }

    @Test
    void shouldExecuteCommandStringUsingExplicitUserWithExplicitTimeout() {
        RecordingUnixSessionProvider provider = new RecordingUnixSessionProvider();
        UnixFacade facade = testFacade(provider);

        CommandResult result = facade.execute(
                "app",
                "oracle",
                "whoami",
                Duration.ofSeconds(5)
        );

        assertEquals(0, result.exitCode());
        assertEquals("app:oracle:whoami", result.stdout());
        assertEquals("", result.stderr());
        assertTrue(provider.lastSession().closed());

        facade.close();
    }

    /**
     * Creates a test facade backed by a recording provider.
     *
     * @param provider recording Unix session provider.
     * @return Unix facade.
     */
    private UnixFacade testFacade(RecordingUnixSessionProvider provider) {
        return new UnixFacade(
                provider,
                new UnixHostSessionRequestFactory(validTargetConfig())
        );
    }

    /**
     * Creates a valid target configuration for tests.
     *
     * @return target configuration.
     */
    private static TargetConfig validTargetConfig() {
        return new TargetConfig(
                "dev",
                Map.of(
                        "app",
                        validHost()
                ),
                new APIConfig(),
                new DBConfig(null, null, null, null, null),
                new UIConfig("https://example.test"),
                new PerfConfig()
        );
    }

    /**
     * Creates a valid host configuration for tests.
     *
     * @return host configuration.
     */
    private static HostConfig validHost() {
        return new HostConfig(
                "application",
                "192.0.2.10",
                22,
                "app",
                Map.of(
                        "app",
                        new UserConfig(
                                "app-user",
                                "password",
                                null
                        ),
                        "oracle",
                        new UserConfig(
                                "oracle-user",
                                "password",
                                null
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
     * Recording Unix session provider used by facade tests.
     */
    private static class RecordingUnixSessionProvider implements UnixSessionProvider {

        private int openCount;
        private UnixHostSessionRequest lastRequest;
        private RecordingUnixHostSession lastSession;
        private boolean closed;

        /**
         * Opens a recording Unix host session.
         *
         * @param request Unix host session request.
         * @return recording Unix host session.
         */
        @Override
        public UnixHostSession open(UnixHostSessionRequest request) {
            openCount++;
            lastRequest = request;
            lastSession = new RecordingUnixHostSession(request);

            return lastSession;
        }

        /**
         * Closes this provider.
         */
        @Override
        public void close() {
            closed = true;
        }

        /**
         * Returns how many sessions were opened.
         *
         * @return open count.
         */
        private int openCount() {
            return openCount;
        }

        /**
         * Returns the last session request.
         *
         * @return last request.
         */
        private UnixHostSessionRequest lastRequest() {
            return lastRequest;
        }

        /**
         * Returns the last opened recording session.
         *
         * @return last session.
         */
        private RecordingUnixHostSession lastSession() {
            return lastSession;
        }

        /**
         * Checks whether the provider was closed.
         *
         * @return {@code true} when closed.
         */
        private boolean closed() {
            return closed;
        }
    }

    /**
     * Recording Unix host session used by facade tests.
     */
    private static class RecordingUnixHostSession implements UnixHostSession {

        private final UnixHostSessionRequest sessionRequest;
        private boolean closed;

        /**
         * Creates a recording Unix host session.
         *
         * @param sessionRequest Unix host session request used to open this session.
         */
        private RecordingUnixHostSession(UnixHostSessionRequest sessionRequest) {
            this.sessionRequest = sessionRequest;
        }

        /**
         * Executes a fake command and records the host/user context in stdout.
         *
         * @param request command execution request.
         * @return fake command result.
         */
        @Override
        public CommandResult execute(UnixCommandRequest request) {
            return new CommandResult(
                    0,
                    sessionRequest.hostAlias()
                            + ":"
                            + sessionRequest.resolvedUserAlias()
                            + ":"
                            + request.command(),
                    ""
            );
        }

        /**
         * Checks whether this fake session is open.
         *
         * @return {@code true} when not closed.
         */
        @Override
        public boolean isOpen() {
            return !closed;
        }

        /**
         * Closes this fake session.
         */
        @Override
        public void close() {
            closed = true;
        }

        /**
         * Checks whether this fake session was closed.
         *
         * @return {@code true} when closed.
         */
        private boolean closed() {
            return closed;
        }
    }
}