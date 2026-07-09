package com.bavykin.qualistrata.unix.session.pool;

import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.session.UnixHostSession;
import com.bavykin.qualistrata.unix.session.UnixHostSessionFactory;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SimpleUnixHostSessionPool}.
 *
 * <p>The tests verify basic in-memory pooling behavior without depending on a
 * real SSH implementation.</p>
 */
class SimpleUnixHostSessionPoolTest {

    @Test
    void shouldCreateSessionWhenPoolIsEmpty() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory();
        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(factory);

        UnixHostSession session = pool.borrow(validRequest());

        assertTrue(session.isOpen());
        assertEquals(1, factory.openCount());
    }

    @Test
    void shouldReuseReleasedSessionForSamePoolKey() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory();
        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(factory);

        UnixHostSession first = pool.borrow(validRequest());

        pool.release(first);

        UnixHostSession second = pool.borrow(validRequest());

        assertEquals(1, factory.openCount());
        assertEquals(first, second);
    }

    @Test
    void shouldCreateDifferentSessionsForDifferentUsers() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory();
        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(factory);

        UnixHostSession first = pool.borrow(validRequest("app"));
        UnixHostSession second = pool.borrow(validRequest("ops"));

        assertEquals(2, factory.openCount());
        assertNotSame(first, second);
    }

    @Test
    void shouldNotReuseClosedReleasedSession() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory();
        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(factory);

        UnixHostSession first = pool.borrow(validRequest());

        first.close();
        pool.release(first);

        UnixHostSession second = pool.borrow(validRequest());

        assertEquals(2, factory.openCount());
        assertNotSame(first, second);
    }

    @Test
    void shouldInvalidateSessionAndCreateReplacement() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory();
        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(factory);

        UnixHostSession first = pool.borrow(validRequest());

        pool.invalidate(first);

        UnixHostSession second = pool.borrow(validRequest());

        assertEquals(2, factory.openCount());
        assertNotSame(first, second);
    }

    @Test
    void shouldFailWhenPoolIsExhaustedAndBorrowTimesOut() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory();
        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(factory);

        UnixHostSessionRequest request = requestWithPool(new UnixPoolConfig(1, 60, 1));

        pool.borrow(request);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> pool.borrow(request)
        );

        assertEquals(
                "Timed out waiting for Unix session from pool for host alias: app",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectReleaseOfForeignSession() {
        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(new RecordingUnixHostSessionFactory());

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> pool.release(new FakeUnixHostSession())
        );

        assertEquals("Session does not belong to this Unix session pool.", exception.getMessage());
    }

    @Test
    void shouldCloseIdleSessionsWhenPoolIsClosed() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory();
        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(factory);

        UnixHostSession session = pool.borrow(validRequest());

        pool.release(session);
        pool.close();

        FakeUnixHostSession physicalSession = factory.createdSession(0);

        assertEquals(1, physicalSession.closeCount());
    }

    @Test
    void shouldRejectBorrowAfterClose() {
        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(new RecordingUnixHostSessionFactory());

        pool.close();

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> pool.borrow(validRequest())
        );

        assertEquals("Unix session pool is already closed.", exception.getMessage());
    }

    @Test
    void shouldDiscardUnhealthyIdleSessionAndCreateReplacement() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory();

        SimpleUnixHostSessionPool pool = new SimpleUnixHostSessionPool(
                factory,
                new com.bavykin.qualistrata.unix.session.UnixHostSessionRequestValidator(),
                session -> false
        );

        UnixHostSession first = pool.borrow(validRequest());

        pool.release(first);

        UnixHostSession second = pool.borrow(validRequest());

        assertEquals(2, factory.openCount());
        assertNotSame(first, second);
        assertEquals(1, factory.createdSession(0).closeCount());
    }

    /**
     * Creates a valid request using the default user.
     *
     * @return valid request.
     */
    private UnixHostSessionRequest validRequest() {
        return validRequest("app");
    }

    /**
     * Creates a valid request using the requested user alias.
     *
     * @param userAlias requested user alias.
     * @return valid request.
     */
    private UnixHostSessionRequest validRequest(String userAlias) {
        return new UnixHostSessionRequest(
                "app",
                validHost(),
                userAlias
        );
    }

    /**
     * Creates a request with custom pool configuration.
     *
     * @param pool pool configuration.
     * @return valid request.
     */
    private UnixHostSessionRequest requestWithPool(UnixPoolConfig pool) {
        HostConfig host = new HostConfig(
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
                pool,
                30
        );

        return new UnixHostSessionRequest("app", host, "app");
    }

    /**
     * Creates a valid host with two users.
     *
     * @return valid host.
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
     * Factory that creates fake sessions and records how many were created.
     */
    private static class RecordingUnixHostSessionFactory implements UnixHostSessionFactory {

        private int openCount;
        private final java.util.List<FakeUnixHostSession> createdSessions = new java.util.ArrayList<>();

        /**
         * Opens a new fake Unix host session.
         *
         * @param request session request.
         * @return new fake session.
         */
        @Override
        public UnixHostSession open(UnixHostSessionRequest request) {
            openCount++;

            FakeUnixHostSession session = new FakeUnixHostSession();
            createdSessions.add(session);

            return session;
        }

        /**
         * @return number of sessions created by this factory.
         */
        private int openCount() {
            return openCount;
        }

        /**
         * Returns a created fake session by index.
         *
         * @param index session index.
         * @return created fake session.
         */
        private FakeUnixHostSession createdSession(int index) {
            return createdSessions.get(index);
        }
    }

    /**
     * Fake physical Unix session used by pool tests.
     */
    private static class FakeUnixHostSession implements UnixHostSession {

        private boolean open = true;
        private int closeCount;

        /**
         * Executes a command and returns a successful result.
         *
         * @param request command execution request.
         * @return successful command result.
         */
        @Override
        public CommandResult execute(UnixCommandRequest request) {
            return new CommandResult(0, "", "");
        }

        /**
         * @return whether the fake session is open.
         */
        @Override
        public boolean isOpen() {
            return open;
        }

        /**
         * Closes the fake session.
         */
        @Override
        public void close() {
            open = false;
            closeCount++;
        }

        /**
         * @return number of close calls.
         */
        private int closeCount() {
            return closeCount;
        }
    }
}