package com.bavykin.qualistrata.unix.session;

/**
 * Factory for opening new physical Unix host sessions.
 *
 * <p>This contract represents creation of a new underlying session. It should
 * not be used directly by tests when pooling is desired. Test and project code
 * should normally depend on {@link UnixSessionProvider}, which can decide
 * whether to open a new session or borrow an existing one from a pool.</p>
 */
public interface UnixHostSessionFactory {

    /**
     * Opens a new Unix host session.
     *
     * @param request session request containing host and user information.
     * @return newly opened session.
     * @throws UnixSessionException if the session cannot be opened.
     */
    UnixHostSession open(UnixHostSessionRequest request);
}