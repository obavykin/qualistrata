package com.bavykin.qualistrata.unix.session;

import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PooledUnixSessionProvider}.
 *
 * <p>The tests verify transparent pooling behavior: consumers receive a normal
 * {@link UnixHostSession}, but closing it releases the borrowed delegate back to
 * the pool.</p>
 */
class PooledUnixSessionProviderTest {

    @Test
    void shouldBorrowSessionFromPool() {
        FakeUnixHostSession delegate = new FakeUnixHostSession();

        RecordingUnixHostSessionPool pool = new RecordingUnixHostSessionPool(delegate);
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        UnixHostSessionRequest request = validRequest();

        UnixHostSession session = provider.open(request);

        assertTrue(session.isOpen());
        assertSame(request, pool.lastBorrowRequest());
        assertEquals(1, pool.borrowCount());
        assertEquals(0, pool.releaseCount());
    }

    @Test
    void shouldDelegateCommandExecutionToBorrowedSession() {
        FakeUnixHostSession delegate = new FakeUnixHostSession();

        RecordingUnixHostSessionPool pool = new RecordingUnixHostSessionPool(delegate);
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        UnixHostSession session = provider.open(validRequest());

        CommandResult result = session.execute(new UnixCommandRequest(
                "hostname",
                Duration.ofSeconds(5)
        ));

        assertEquals(0, result.exitCode());
        assertEquals("ok", result.stdout());
        assertEquals(1, delegate.executeCount());
    }

    @Test
    void shouldReleaseSessionBackToPoolOnClose() {
        FakeUnixHostSession delegate = new FakeUnixHostSession();

        RecordingUnixHostSessionPool pool = new RecordingUnixHostSessionPool(delegate);
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        UnixHostSession session = provider.open(validRequest());

        session.close();

        assertFalse(session.isOpen());
        assertSame(delegate, pool.lastReleasedSession());
        assertEquals(1, pool.releaseCount());
        assertEquals(0, delegate.closeCount());
    }

    @Test
    void shouldReleaseSessionOnlyOnceWhenClosedMultipleTimes() {
        FakeUnixHostSession delegate = new FakeUnixHostSession();

        RecordingUnixHostSessionPool pool = new RecordingUnixHostSessionPool(delegate);
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        UnixHostSession session = provider.open(validRequest());

        session.close();
        session.close();

        assertEquals(1, pool.releaseCount());
    }

    @Test
    void shouldRejectCommandExecutionAfterClose() {
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(
                new RecordingUnixHostSessionPool(new FakeUnixHostSession())
        );

        UnixHostSession session = provider.open(validRequest());
        session.close();

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> session.execute(new UnixCommandRequest("hostname", Duration.ofSeconds(5)))
        );

        assertEquals("Pooled Unix session is already closed.", exception.getMessage());
    }

    @Test
    void shouldValidateRequestBeforeBorrowingSession() {
        RecordingUnixHostSessionPool pool = new RecordingUnixHostSessionPool(new FakeUnixHostSession());
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> provider.open(new UnixHostSessionRequest(" ", validHost(), "app"))
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
        assertEquals(0, pool.borrowCount());
    }

    @Test
    void shouldFailWhenPoolReturnsNullSession() {
        RecordingUnixHostSessionPool pool = new RecordingUnixHostSessionPool(null);
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> provider.open(validRequest())
        );

        assertEquals("Unix host session pool returned null session.", exception.getMessage());
    }

    @Test
    void shouldFailWhenPoolIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new PooledUnixSessionProvider(null)
        );

        assertEquals("Unix host session pool must not be null.", exception.getMessage());
    }

    @Test
    void shouldCloseUnderlyingPoolWhenProviderIsClosed() {
        RecordingUnixHostSessionPool pool = new RecordingUnixHostSessionPool(new FakeUnixHostSession());
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        provider.close();

        assertEquals(1, pool.closeCount());
    }

    @Test
    void shouldInvalidateSessionThroughPool() {
        FakeUnixHostSession delegate = new FakeUnixHostSession();

        RecordingUnixHostSessionPool pool = new RecordingUnixHostSessionPool(delegate);
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        UnixHostSession session = provider.open(validRequest());

        InvalidatableUnixHostSession invalidatableSession =
                assertInstanceOf(InvalidatableUnixHostSession.class, session);

        invalidatableSession.invalidate();

        assertFalse(session.isOpen());
        assertSame(delegate, pool.lastInvalidatedSession());
        assertEquals(1, pool.invalidateCount());
        assertEquals(0, pool.releaseCount());
    }

    @Test
    void shouldInvalidateSessionOnlyOnceWhenInvalidatedMultipleTimes() {
        FakeUnixHostSession delegate = new FakeUnixHostSession();

        RecordingUnixHostSessionPool pool = new RecordingUnixHostSessionPool(delegate);
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        UnixHostSession session = provider.open(validRequest());

        InvalidatableUnixHostSession invalidatableSession =
                assertInstanceOf(InvalidatableUnixHostSession.class, session);

        invalidatableSession.invalidate();
        invalidatableSession.invalidate();

        assertEquals(1, pool.invalidateCount());
    }

    @Test
    void shouldNotReleaseSessionAfterInvalidation() {
        FakeUnixHostSession delegate = new FakeUnixHostSession();

        RecordingUnixHostSessionPool pool = new RecordingUnixHostSessionPool(delegate);
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(pool);

        UnixHostSession session = provider.open(validRequest());

        InvalidatableUnixHostSession invalidatableSession =
                assertInstanceOf(InvalidatableUnixHostSession.class, session);

        invalidatableSession.invalidate();
        session.close();

        assertEquals(1, pool.invalidateCount());
        assertEquals(0, pool.releaseCount());
    }

    @Test
    void shouldRejectCommandExecutionAfterInvalidation() {
        PooledUnixSessionProvider provider = new PooledUnixSessionProvider(
                new RecordingUnixHostSessionPool(new FakeUnixHostSession())
        );

        UnixHostSession session = provider.open(validRequest());

        InvalidatableUnixHostSession invalidatableSession =
                assertInstanceOf(InvalidatableUnixHostSession.class, session);

        invalidatableSession.invalidate();

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> session.execute(new UnixCommandRequest("hostname", Duration.ofSeconds(5)))
        );

        assertEquals("Pooled Unix session is already invalidated.", exception.getMessage());
    }

    /**
     * Creates a valid Unix host session request.
     *
     * @return valid session request.
     */
    private UnixHostSessionRequest validRequest() {
        return new UnixHostSessionRequest(
                "app",
                validHost(),
                "app"
        );
    }

    /**
     * Creates a valid host configuration for pooled provider tests.
     *
     * @return valid host configuration.
     */
    private HostConfig validHost() {
        return new HostConfig(
                "application",
                "app.demo.test",
                22,
                "app",
                Map.of("app", new UserConfig("app", null, "/home/app/.ssh/id_rsa")),
                null,
                null,
                new UnixPoolConfig(5, 60, 10),
                30
        );
    }

    /**
     * Test pool that records lifecycle calls and returns a predefined session.
     */
    private static class RecordingUnixHostSessionPool implements UnixHostSessionPool {

        private final UnixHostSession session;

        private UnixHostSessionRequest lastBorrowRequest;
        private UnixHostSession lastReleasedSession;
        private UnixHostSession lastInvalidatedSession;
        private int borrowCount;
        private int releaseCount;
        private int invalidateCount;
        private int closeCount;

        /**
         * Creates a recording pool.
         *
         * @param session session returned from {@link #borrow}.
         */
        private RecordingUnixHostSessionPool(UnixHostSession session) {
            this.session = session;
        }

        /**
         * Records the request and returns the configured session.
         *
         * @param request session request.
         * @return configured session.
         */
        @Override
        public UnixHostSession borrow(UnixHostSessionRequest request) {
            lastBorrowRequest = request;
            borrowCount++;

            return session;
        }

        /**
         * Records released session.
         *
         * @param session session to return.
         */
        @Override
        public void release(UnixHostSession session) {
            lastReleasedSession = session;
            releaseCount++;
        }

        /**
         * Records invalidated session.
         *
         * @param session session to invalidate.
         */
        @Override
        public void invalidate(UnixHostSession session) {
            lastInvalidatedSession = session;
            invalidateCount++;
        }

        /**
         * Records pool close.
         */
        @Override
        public void close() {
            closeCount++;
        }

        /**
         * @return last invalidated session.
         */
        private UnixHostSession lastInvalidatedSession() {
            return lastInvalidatedSession;
        }

        /**
         * @return number of invalidate calls.
         */
        private int invalidateCount() {
            return invalidateCount;
        }

        /**
         * @return last borrow request.
         */
        private UnixHostSessionRequest lastBorrowRequest() {
            return lastBorrowRequest;
        }

        /**
         * @return last released session.
         */
        private UnixHostSession lastReleasedSession() {
            return lastReleasedSession;
        }

        /**
         * @return number of borrow calls.
         */
        private int borrowCount() {
            return borrowCount;
        }

        /**
         * @return number of release calls.
         */
        private int releaseCount() {
            return releaseCount;
        }

        /**
         * @return number of close calls.
         */
        private int closeCount() {
            return closeCount;
        }
    }

    /**
     * Minimal fake session used by pooled provider tests.
     */
    private static class FakeUnixHostSession implements UnixHostSession {

        private int executeCount;
        private int closeCount;
        private boolean open = true;

        /**
         * Records command execution and returns a successful result.
         *
         * @param request command execution request.
         * @return successful command result.
         */
        @Override
        public CommandResult execute(UnixCommandRequest request) {
            executeCount++;

            return new CommandResult(0, "ok", "");
        }

        /**
         * @return whether this fake session is open.
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
         * @return number of execute calls.
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