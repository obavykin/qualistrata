package com.bavykin.qualistrata.unix.facade;

import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.session.UnixHostSession;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionException;
import com.bavykin.qualistrata.unix.session.UnixSessionProvider;
import com.bavykin.qualistrata.unix.session.factory.UnixSessionProviderFactory;
import com.bavykin.qualistrata.unix.session.factory.UnixSessionProviderType;
import com.bavykin.qualistrata.unix.session.request.UnixHostSessionRequestFactory;

/**
 * High-level facade for Unix session operations.
 *
 * <p>The facade combines {@link UnixHostSessionRequestFactory} and
 * {@link UnixSessionProvider}. It allows consuming projects to open Unix
 * sessions by logical host alias instead of manually resolving host
 * configuration and assembling session requests.</p>
 *
 * <p>The facade owns the provided {@link UnixSessionProvider}. Closing the
 * facade closes the underlying provider and its provider-level resources.</p>
 */
public class UnixFacade implements AutoCloseable {

    private final UnixSessionProvider sessionProvider;
    private final UnixHostSessionRequestFactory requestFactory;

    /**
     * Creates a pooled Unix facade backed by the default JSch SSH transport.
     *
     * @param targetConfig resolved target configuration.
     * @return Unix facade with pooled session provider.
     */
    public static UnixFacade pooled(TargetConfig targetConfig) {
        return of(targetConfig, UnixSessionProviderType.POOLED);
    }

    /**
     * Creates a direct Unix facade backed by the default JSch SSH transport.
     *
     * @param targetConfig resolved target configuration.
     * @return Unix facade with direct session provider.
     */
    public static UnixFacade direct(TargetConfig targetConfig) {
        return of(targetConfig, UnixSessionProviderType.DIRECT);
    }

    /**
     * Creates a Unix facade backed by the default JSch SSH transport and the
     * requested provider type.
     *
     * @param targetConfig resolved target configuration.
     * @param providerType Unix session provider type.
     * @return Unix facade.
     */
    public static UnixFacade of(
            TargetConfig targetConfig,
            UnixSessionProviderType providerType
    ) {
        UnixHostSessionRequestFactory requestFactory = new UnixHostSessionRequestFactory(targetConfig);
        UnixSessionProvider sessionProvider = new UnixSessionProviderFactory().create(providerType);

        return new UnixFacade(sessionProvider, requestFactory);
    }

    /**
     * Creates a Unix facade using explicitly provided collaborators.
     *
     * <p>This constructor is useful for tests, custom providers, or consumers
     * that need to control the Unix session provider lifecycle directly.</p>
     *
     * @param sessionProvider Unix session provider owned by this facade.
     * @param requestFactory Unix host session request factory.
     */
    public UnixFacade(
            UnixSessionProvider sessionProvider,
            UnixHostSessionRequestFactory requestFactory
    ) {
        if (sessionProvider == null) {
            throw new UnixSessionException("Unix session provider must not be null.");
        }

        if (requestFactory == null) {
            throw new UnixSessionException("Unix host session request factory must not be null.");
        }

        this.sessionProvider = sessionProvider;
        this.requestFactory = requestFactory;
    }

    /**
     * Opens a Unix session for the given host alias using the host default user.
     *
     * @param hostAlias logical host alias from target configuration.
     * @return opened Unix host session.
     */
    public UnixHostSession open(String hostAlias) {
        return open(hostAlias, null);
    }

    /**
     * Opens a Unix session for the given host alias and user alias.
     *
     * @param hostAlias logical host alias from target configuration.
     * @param userAlias optional user alias.
     * @return opened Unix host session.
     */
    public UnixHostSession open(String hostAlias, String userAlias) {
        UnixHostSessionRequest request = requestFactory.create(hostAlias, userAlias);

        return sessionProvider.open(request);
    }

    /**
     * Executes a single command on the given host using the host default user.
     *
     * <p>The borrowed session is closed automatically after command execution.
     * For pooled providers this returns the physical session to the pool.</p>
     *
     * @param hostAlias logical host alias from target configuration.
     * @param request command execution request.
     * @return command execution result.
     */
    public CommandResult execute(String hostAlias, UnixCommandRequest request) {
        return execute(hostAlias, null, request);
    }

    /**
     * Executes a single command on the given host using an explicit user alias.
     *
     * <p>The borrowed session is closed automatically after command execution.
     * For pooled providers this returns the physical session to the pool.</p>
     *
     * @param hostAlias logical host alias from target configuration.
     * @param userAlias optional user alias.
     * @param request command execution request.
     * @return command execution result.
     */
    public CommandResult execute(
            String hostAlias,
            String userAlias,
            UnixCommandRequest request
    ) {
        try (UnixHostSession session = open(hostAlias, userAlias)) {
            return session.execute(request);
        }
    }

    /**
     * Closes the underlying Unix session provider.
     */
    @Override
    public void close() {
        sessionProvider.close();
    }
}