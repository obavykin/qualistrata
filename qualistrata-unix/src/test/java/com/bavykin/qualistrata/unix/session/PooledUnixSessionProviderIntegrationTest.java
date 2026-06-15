package com.bavykin.qualistrata.unix.session;

import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.session.pool.SimpleUnixHostSessionPool;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration-style unit tests for {@link PooledUnixSessionProvider}.
 *
 * <p>The tests verify that the consumer-facing pooled provider works correctly
 * together with the simple in-memory session pool and a session factory. This
 * protects the lifecycle contract between two pooling layers:</p>
 *
 * <ul>
 *     <li>{@link PooledUnixSessionProvider} returns a consumer-facing wrapper;</li>
 *     <li>{@link SimpleUnixHostSessionPool} owns reusable session handles;</li>
 *     <li>closing the consumer wrapper must release the pool-owned handle back
 *     to the pool.</li>
 * </ul>
 */
class PooledUnixSessionProviderIntegrationTest {

    @Test
    void shouldReusePhysicalSessionWhenConsumerWrapperIsClosed() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory();

        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(factory);
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        UnixHostSessionRequest request = validRequest();

        try (UnixHostSession firstSession = provider.open(request)) {
            CommandResult result = firstSession.execute(new UnixCommandRequest(
                    "hostname",
                    Duration.ofSeconds(5)
            ));

            assertEquals(0, result.exitCode());
            assertEquals("session-1", result.stdout());
        }

        try (UnixHostSession secondSession = provider.open(request)) {
            CommandResult result = secondSession.execute(new UnixCommandRequest(
                    "hostname",
                    Duration.ofSeconds(5)
            ));

            assertEquals(0, result.exitCode());
            assertEquals("session-1", result.stdout());
        }

        assertEquals(1, factory.openCount());
        assertEquals(2, factory.createdSession(0).executeCount());
        assertEquals(0, factory.createdSession(0).closeCount());
    }

    @Test
    void shouldCreateSeparatePhysicalSessionsForDifferentUsers() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory();

        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(factory);
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        try (UnixHostSession appSession = provider.open(validRequest("app"))) {
            CommandResult result = appSession.execute(new UnixCommandRequest(
                    "whoami",
                    Duration.ofSeconds(5)
            ));

            assertEquals("session-1", result.stdout());
        }

        try (UnixHostSession opsSession = provider.open(validRequest("ops"))) {
            CommandResult result = opsSession.execute(new UnixCommandRequest(
                    "whoami",
                    Duration.ofSeconds(5)
            ));

            assertEquals("session-2", result.stdout());
        }

        assertEquals(2, factory.openCount());
    }

    @Test
    void shouldCloseIdlePhysicalSessionsWhenProviderIsClosed() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory();

        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(factory);
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        try (UnixHostSession session = provider.open(validRequest())) {
            assertTrue(session.isOpen());
        }

        provider.close();

        assertEquals(1, factory.openCount());
        assertEquals(1, factory.createdSession(0).closeCount());
    }

    /**
     * Creates a valid Unix host session request using the default user.
     *
     * @return valid session request.
     */
    private UnixHostSessionRequest validRequest() {
        return validRequest("app");
    }

    /**
     * Creates a valid Unix host session request using the requested user alias.
     *
     * @param userAlias requested logical user alias.
     * @return valid session request.
     */
    private UnixHostSessionRequest validRequest(String userAlias) {
        return new UnixHostSessionRequest(
                "app",
                validHost(),
                userAlias
        );
    }

    /**
     * Creates a valid host configuration with two users and pool settings.
     *
     * @return valid host configuration.
     */
    private HostConfig validHost() {
        return new HostConfig(
                "application",
                "app.demo.test",
                22,
                "app",
                Map.of(
                        "app", new UserConfig("app", null, "/home/app/.ssh/id_rsa"),
                        "ops", new UserConfig("ops", null, "/home/ops/.ssh/id_rsa")
                ),
                null,
                null,
                new UnixPoolConfig(2, 60, 1),
                30
        );
    }

    /**
     * Factory that creates fake physical sessions and records how many sessions
     * were created.
     */
    private static class RecordingUnixHostSessionFactory implements UnixHostSessionFactory {

        private int openCount;
        private final java.util.List<RecordingUnixHostSession> createdSessions = new java.util.ArrayList<>();

        /**
         * Opens a new fake physical session.
         *
         * @param request session request.
         * @return newly created fake physical session.
         */
        @Override
        public UnixHostSession open(UnixHostSessionRequest request) {
            openCount++;

            RecordingUnixHostSession session = new RecordingUnixHostSession("session-" + openCount);
            createdSessions.add(session);

            return session;
        }

        /**
         * @return number of physical sessions created by this factory.
         */
        private int openCount() {
            return openCount;
        }

        /**
         * Returns a created session by index.
         *
         * @param index created session index.
         * @return created fake physical session.
         */
        private RecordingUnixHostSession createdSession(int index) {
            return createdSessions.get(index);
        }
    }

    /**
     * Fake physical Unix session used to verify reuse through provider and pool.
     */
    private static class RecordingUnixHostSession implements UnixHostSession {

        private final String stdout;
        private boolean open = true;
        private int executeCount;
        private int closeCount;

        /**
         * Creates a recording physical session.
         *
         * @param stdout stdout returned by this session.
         */
        private RecordingUnixHostSession(String stdout) {
            this.stdout = stdout;
        }

        /**
         * Records command execution and returns a successful command result.
         *
         * @param request command execution request.
         * @return successful command result with session-specific stdout.
         */
        @Override
        public CommandResult execute(UnixCommandRequest request) {
            executeCount++;

            return new CommandResult(0, stdout, "");
        }

        /**
         * @return whether this fake physical session is open.
         */
        @Override
        public boolean isOpen() {
            return open;
        }

        /**
         * Closes this fake physical session.
         */
        @Override
        public void close() {
            open = false;
            closeCount++;
        }

        /**
         * @return number of executed commands.
         */
        private int executeCount() {
            return executeCount;
        }

        /**
         * @return number of close calls.
         */
        private int closeCount() {
            return closeCount;
        }
    }
}