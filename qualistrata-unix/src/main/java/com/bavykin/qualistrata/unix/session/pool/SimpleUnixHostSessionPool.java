package com.bavykin.qualistrata.unix.session.pool;

import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.unix.session.*;
import com.bavykin.qualistrata.unix.session.health.OpenUnixSessionHealthCheck;
import com.bavykin.qualistrata.unix.session.health.UnixSessionHealthCheck;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Simple in-memory implementation of {@link UnixHostSessionPool}.
 *
 * <p>The pool reuses Unix sessions by connection key. A key is derived from
 * host alias, physical host address, port, and resolved user alias. Sessions
 * from one host/user pair must never be reused for another host/user pair.</p>
 *
 * <p>This implementation is intentionally small and dependency-free. It is
 * suitable as a baseline implementation and as a reference for future SSH/JSch
 * pooling behavior.</p>
 *
 * <p>The pool is synchronized internally. It is designed for correctness and
 * predictable behavior first, not for maximum throughput.</p>
 */
public class SimpleUnixHostSessionPool implements UnixHostSessionPool {

    private static final int DEFAULT_SIZE = 1;
    private static final int DEFAULT_BORROW_TIMEOUT_SECONDS = 30;

    private final UnixHostSessionFactory sessionFactory;
    private final UnixHostSessionRequestValidator requestValidator;

    private final Map<UnixSessionPoolKey, Queue<UnixHostSession>> idleSessions = new HashMap<>();
    private final Map<UnixSessionPoolKey, Integer> totalSessions = new HashMap<>();

    private final UnixSessionHealthCheck healthCheck;

    private boolean closed;

    /**
     * Creates a simple Unix host session pool.
     *
     * @param sessionFactory factory used to create new physical sessions.
     */
    public SimpleUnixHostSessionPool(UnixHostSessionFactory sessionFactory) {
        this(
                sessionFactory,
                new UnixHostSessionRequestValidator(),
                new OpenUnixSessionHealthCheck()
        );
    }

    /**
     * Creates a simple Unix host session pool with explicitly provided
     * collaborators.
     *
     * @param sessionFactory factory used to create new physical sessions.
     * @param requestValidator validator used before borrowing sessions.
     * @param healthCheck health check used before reusing idle sessions.
     */
    SimpleUnixHostSessionPool(
            UnixHostSessionFactory sessionFactory,
            UnixHostSessionRequestValidator requestValidator,
            UnixSessionHealthCheck healthCheck
    ) {
        if (sessionFactory == null) {
            throw new UnixSessionException("Unix host session factory must not be null.");
        }

        if (requestValidator == null) {
            throw new UnixSessionException("Unix host session request validator must not be null.");
        }

        if (healthCheck == null) {
            throw new UnixSessionException("Unix session health check must not be null.");
        }

        this.sessionFactory = sessionFactory;
        this.requestValidator = requestValidator;
        this.healthCheck = healthCheck;
    }

    /**
     * Borrows a session for the requested host and user.
     *
     * <p>If an idle session exists for the same connection key, it is reused. If
     * the pool has capacity, a new session is created. Otherwise, the method
     * waits until another thread releases a session or the borrow timeout
     * expires.</p>
     *
     * @param request session request.
     * @return borrowed session.
     */
    @Override
    public synchronized UnixHostSession borrow(UnixHostSessionRequest request) {
        requestValidator.validate(request);
        ensurePoolOpen();

        UnixSessionPoolKey key = UnixSessionPoolKey.from(request);
        int maxSize = poolSize(request.host().pool());
        long deadline = System.nanoTime() + borrowTimeout(request.host().pool()).toNanos();

        while (true) {
            UnixHostSession idle = pollOpenIdleSession(key);

            if (idle != null) {
                return idle;
            }

            if (totalSessions.getOrDefault(key, 0) < maxSize) {
                return createSession(key, request);
            }

            long remainingNanos = deadline - System.nanoTime();

            if (remainingNanos <= 0) {
                throw new UnixSessionException("Timed out waiting for Unix session from pool for host alias: "
                        + request.hostAlias());
            }

            waitForRelease(remainingNanos);
            ensurePoolOpen();
        }
    }

    /**
     * Returns a healthy session to the pool.
     *
     * <p>Closed sessions are not returned to the idle queue and are treated as
     * invalidated resources.</p>
     *
     * @param session session to return.
     */
    @Override
    public synchronized void release(UnixHostSession session) {
        ensurePoolOpen();

        PooledSessionHandle handle = requirePoolHandle(session);

        if (!handle.session().isOpen()) {
            decrementTotal(handle.key());
            notifyAll();
            return;
        }

        idleSessions
                .computeIfAbsent(handle.key(), ignored -> new ArrayDeque<>())
                .offer(handle);

        notifyAll();
    }

    /**
     * Invalidates a broken or unsafe session.
     *
     * <p>Invalidated sessions are physically closed and removed from capacity
     * accounting.</p>
     *
     * @param session session to invalidate.
     */
    @Override
    public synchronized void invalidate(UnixHostSession session) {
        PooledSessionHandle handle = requirePoolHandle(session);

        closeQuietly(handle.session());
        decrementTotal(handle.key());

        notifyAll();
    }

    /**
     * Closes the pool and all idle sessions.
     *
     * <p>Borrowed sessions cannot be forcibly discovered by this simple pool,
     * but further borrow/release operations are rejected after closure.</p>
     */
    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }

        closed = true;

        idleSessions.values().forEach(queue -> queue.forEach(this::closeQuietly));
        idleSessions.clear();
        totalSessions.clear();

        notifyAll();
    }

    /**
     * Creates a new physical session and updates capacity accounting.
     *
     * @param key pool key.
     * @param request session request.
     * @return created session wrapped with pool metadata.
     */
    private UnixHostSession createSession(UnixSessionPoolKey key, UnixHostSessionRequest request) {
        UnixHostSession session = sessionFactory.open(request);

        if (session == null) {
            throw new UnixSessionException("Unix host session factory returned null session.");
        }

        totalSessions.merge(key, 1, Integer::sum);

        return new PooledSessionHandle(key, session);
    }

    /**
     * Polls open idle sessions for the key and discards closed ones.
     *
     * @param key pool key.
     * @return open idle session handle, or {@code null} when none is available.
     */
    private UnixHostSession pollOpenIdleSession(UnixSessionPoolKey key) {
        Queue<UnixHostSession> queue = idleSessions.get(key);

        while (queue != null && !queue.isEmpty()) {
            UnixHostSession session = queue.poll();
            PooledSessionHandle handle = requirePoolHandle(session);

            if (healthCheck.isHealthy(handle.session())) {
                return handle;
            }

            closeQuietly(handle.session());
            decrementTotal(key);
        }

        return null;
    }

    /**
     * Waits for a released session or capacity change.
     *
     * @param remainingNanos remaining time in nanoseconds.
     */
    private void waitForRelease(long remainingNanos) {
        try {
            long millis = Math.max(1, Duration.ofNanos(remainingNanos).toMillis());

            wait(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new UnixSessionException("Interrupted while waiting for Unix session from pool.", exception);
        }
    }

    /**
     * Extracts pool metadata from a session handle.
     *
     * @param session session passed to release or invalidate.
     * @return pool session handle.
     */
    private PooledSessionHandle requirePoolHandle(UnixHostSession session) {
        if (!(session instanceof PooledSessionHandle handle)) {
            throw new UnixSessionException("Session does not belong to this Unix session pool.");
        }

        return handle;
    }

    /**
     * Ensures the pool is still open.
     */
    private void ensurePoolOpen() {
        if (closed) {
            throw new UnixSessionException("Unix session pool is already closed.");
        }
    }

    /**
     * Resolves pool size from configuration.
     *
     * @param pool pool configuration.
     * @return configured or default pool size.
     */
    private int poolSize(UnixPoolConfig pool) {
        if (pool == null || pool.size() == null) {
            return DEFAULT_SIZE;
        }

        return pool.size();
    }

    /**
     * Resolves borrow timeout from configuration.
     *
     * @param pool pool configuration.
     * @return configured or default borrow timeout.
     */
    private Duration borrowTimeout(UnixPoolConfig pool) {
        if (pool == null || pool.borrowTimeoutSeconds() == null) {
            return Duration.ofSeconds(DEFAULT_BORROW_TIMEOUT_SECONDS);
        }

        return Duration.ofSeconds(pool.borrowTimeoutSeconds());
    }

    /**
     * Decrements total session count for a key.
     *
     * @param key pool key.
     */
    private void decrementTotal(UnixSessionPoolKey key) {
        int current = totalSessions.getOrDefault(key, 0);

        if (current <= 1) {
            totalSessions.remove(key);
            return;
        }

        totalSessions.put(key, current - 1);
    }

    /**
     * Closes a session and suppresses close exceptions.
     *
     * @param session session to close.
     */
    private void closeQuietly(UnixHostSession session) {
        try {
            session.close();
        } catch (RuntimeException ignored) {
            // Pool shutdown and invalidation should continue closing remaining sessions.
        }
    }
}