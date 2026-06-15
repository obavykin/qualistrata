package com.bavykin.qualistrata.unix.session;

/**
 * Pool for reusing Unix host sessions.
 *
 * <p>Session reuse is a first-class architectural requirement for QualiStrata
 * because opening remote Unix or SSH connections can be expensive. This
 * contract keeps pooling explicit while allowing consumers to depend on the
 * simpler {@link UnixSessionProvider} API.</p>
 *
 * <p>Implementations may pool sessions by host alias, host address, user alias,
 * or any other safe connection key.</p>
 */
public interface UnixHostSessionPool extends AutoCloseable {

    /**
     * Borrows a session for the requested host and user.
     *
     * @param request session request.
     * @return borrowed session.
     * @throws UnixSessionException if a session cannot be borrowed.
     */
    UnixHostSession borrow(UnixHostSessionRequest request);

    /**
     * Returns a healthy session to the pool.
     *
     * @param session session to return.
     * @throws UnixSessionException if the session cannot be returned.
     */
    void release(UnixHostSession session);

    /**
     * Invalidates a broken or unsafe session.
     *
     * <p>Invalidated sessions must not be returned to the reusable pool.</p>
     *
     * @param session session to invalidate.
     */
    void invalidate(UnixHostSession session);

    /**
     * Closes the pool and releases all underlying resources.
     */
    @Override
    void close();
}