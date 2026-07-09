package com.bavykin.qualistrata.unix.session;

/**
 * Consumer-facing provider of Unix host sessions.
 *
 * <p>Project and test code should normally depend on this contract instead of
 * depending directly on SSH clients, factories, or pools. A provider may use
 * direct session creation, pooled session reuse, local process execution, mocks,
 * or any future implementation strategy.</p>
 *
 * <p>This keeps pooling transparent for consumers:</p>
 *
 * <pre>
 * try (UnixHostSession session = sessionProvider.open(request)) {
 *     session.execute(commandRequest);
 * }
 * </pre>
 */
public interface UnixSessionProvider extends AutoCloseable {

    /**
     * Opens a session for the requested host and user.
     *
     * <p>Depending on implementation, this may create a new physical session or
     * borrow an existing reusable session from a pool.</p>
     *
     * @param request session request.
     * @return opened session.
     * @throws UnixSessionException if a session cannot be opened or borrowed.
     */
    UnixHostSession open(UnixHostSessionRequest request);

    /**
     * Closes this provider and releases any underlying resources.
     *
     * <p>Direct providers may have nothing to close. Pooled providers should
     * close the underlying pool.</p>
     */
    @Override
    void close();
}