package com.bavykin.qualistrata.unix.session;

/**
 * Direct Unix session provider that opens a new physical session for each
 * request.
 *
 * <p>This provider does not use pooling. Each call to {@link #open} validates
 * the request and delegates to {@link UnixHostSessionFactory} to create a new
 * session.</p>
 *
 * <p>The provider is useful for diagnostics, simple projects, tests, fallback
 * modes, and troubleshooting scenarios where pooling should be excluded.</p>
 */
public class DirectUnixSessionProvider implements UnixSessionProvider {

    private final UnixHostSessionFactory sessionFactory;
    private final UnixHostSessionRequestValidator requestValidator;

    /**
     * Creates a direct Unix session provider.
     *
     * @param sessionFactory factory used to open new physical sessions.
     */
    public DirectUnixSessionProvider(UnixHostSessionFactory sessionFactory) {
        this(sessionFactory, new UnixHostSessionRequestValidator());
    }

    /**
     * Creates a direct Unix session provider with explicitly provided
     * collaborators.
     *
     * <p>This constructor keeps the provider testable and allows validation
     * behavior to be customized in future without changing the public API.</p>
     *
     * @param sessionFactory factory used to open new physical sessions.
     * @param requestValidator validator used before opening a session.
     */
    DirectUnixSessionProvider(
            UnixHostSessionFactory sessionFactory,
            UnixHostSessionRequestValidator requestValidator
    ) {
        if (sessionFactory == null) {
            throw new UnixSessionException("Unix host session factory must not be null.");
        }

        if (requestValidator == null) {
            throw new UnixSessionException("Unix host session request validator must not be null.");
        }

        this.sessionFactory = sessionFactory;
        this.requestValidator = requestValidator;
    }

    /**
     * Opens a new physical Unix host session.
     *
     * @param request session request.
     * @return newly opened session.
     * @throws UnixSessionException if the request is invalid or the factory
     *                              cannot open a session.
     */
    @Override
    public UnixHostSession open(UnixHostSessionRequest request) {
        requestValidator.validate(request);

        return sessionFactory.open(request);
    }

    /**
     * Closes this provider.
     *
     * <p>The direct provider does not own reusable resources. Sessions opened by
     * this provider are owned by callers and should be closed by callers,
     * normally through try-with-resources.</p>
     */
    @Override
    public void close() {
        // No shared resources are owned by the direct provider.
    }
}