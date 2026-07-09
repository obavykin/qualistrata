package com.bavykin.qualistrata.unix.session;

/**
 * Unix session provider that borrows sessions from a {@link UnixHostSessionPool}.
 *
 * <p>This provider makes pooling transparent for consumers. Test and project
 * code still works with the simple {@link UnixSessionProvider} API and uses
 * try-with-resources. Closing the returned session releases the underlying
 * borrowed session back to the pool instead of directly closing the physical
 * connection.</p>
 *
 * <p>Broken sessions must be invalidated rather than released. This class
 * provides the basic pooled lifecycle wrapper; future SSH implementations may
 * add richer health checks and automatic invalidation rules.</p>
 */
public class PooledUnixSessionProvider implements UnixSessionProvider {

    private final UnixHostSessionPool sessionPool;
    private final UnixHostSessionRequestValidator requestValidator;

    /**
     * Creates a pooled Unix session provider.
     *
     * @param sessionPool pool used to borrow and release sessions.
     */
    public PooledUnixSessionProvider(UnixHostSessionPool sessionPool) {
        this(sessionPool, new UnixHostSessionRequestValidator());
    }

    /**
     * Creates a pooled Unix session provider with explicitly provided
     * collaborators.
     *
     * @param sessionPool pool used to borrow and release sessions.
     * @param requestValidator validator used before borrowing a session.
     */
    PooledUnixSessionProvider(
            UnixHostSessionPool sessionPool,
            UnixHostSessionRequestValidator requestValidator
    ) {
        if (sessionPool == null) {
            throw new UnixSessionException("Unix host session pool must not be null.");
        }

        if (requestValidator == null) {
            throw new UnixSessionException("Unix host session request validator must not be null.");
        }

        this.sessionPool = sessionPool;
        this.requestValidator = requestValidator;
    }

    /**
     * Borrows a session from the pool and wraps it in a consumer-facing session.
     *
     * <p>Closing the returned wrapper releases the borrowed session back to the
     * pool.</p>
     *
     * @param request session request.
     * @return pooled session wrapper.
     * @throws UnixSessionException if the request is invalid or a session cannot
     *                              be borrowed.
     */
    @Override
    public UnixHostSession open(UnixHostSessionRequest request) {
        requestValidator.validate(request);

        UnixHostSession borrowedSession = sessionPool.borrow(request);

        if (borrowedSession == null) {
            throw new UnixSessionException("Unix host session pool returned null session.");
        }

        return new PooledUnixHostSession(borrowedSession, sessionPool);
    }

    /**
     * Closes this provider and releases pool resources.
     */
    @Override
    public void close() {
        sessionPool.close();
    }
}