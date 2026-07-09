package com.bavykin.qualistrata.unix.ssh;

import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.unix.session.UnixHostSession;
import com.bavykin.qualistrata.unix.session.UnixHostSessionFactory;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequestValidator;

import java.time.Duration;

/**
 * Factory that creates SSH-backed Unix host sessions.
 *
 * <p>The factory converts generic QualiStrata host/user configuration into a
 * low-level {@link SshConnectionRequest}, opens an SSH connection through
 * {@link SshConnectionFactory}, and wraps it as {@link SshUnixHostSession}.</p>
 *
 * <p>This class still does not depend on any concrete SSH library. Real SSH
 * transport implementation is provided by {@link SshConnectionFactory}.</p>
 */
public class SshUnixHostSessionFactory implements UnixHostSessionFactory {

    private static final int DEFAULT_SSH_PORT = 22;

    private final SshConnectionFactory connectionFactory;
    private final UnixHostSessionRequestValidator requestValidator;

    /**
     * Creates an SSH Unix host session factory.
     *
     * @param connectionFactory factory used to open low-level SSH connections.
     */
    public SshUnixHostSessionFactory(SshConnectionFactory connectionFactory) {
        this(
                connectionFactory,
                new UnixHostSessionRequestValidator()
        );
    }

    /**
     * Creates an SSH Unix host session factory with explicitly provided
     * collaborators.
     *
     * @param connectionFactory factory used to open low-level SSH connections.
     * @param requestValidator validator used before opening SSH-backed sessions.
     */
    SshUnixHostSessionFactory(
            SshConnectionFactory connectionFactory,
            UnixHostSessionRequestValidator requestValidator
    ) {
        if (connectionFactory == null) {
            throw new SshConnectionException("SSH connection factory must not be null.");
        }

        if (requestValidator == null) {
            throw new SshConnectionException("Unix host session request validator must not be null.");
        }

        this.connectionFactory = connectionFactory;
        this.requestValidator = requestValidator;
    }

    /**
     * Opens a new SSH-backed Unix host session.
     *
     * @param request session request containing host and user information.
     * @return SSH-backed Unix host session.
     */
    @Override
    public UnixHostSession open(UnixHostSessionRequest request) {
        requestValidator.validate(request);

        SshConnectionRequest connectionRequest = toConnectionRequest(request);
        SshConnection connection = connectionFactory.open(connectionRequest);

        if (connection == null) {
            throw new SshConnectionException("SSH connection factory returned null connection.");
        }

        return new SshUnixHostSession(connection);
    }

    /**
     * Converts a Unix host session request into a low-level SSH connection
     * request.
     *
     * @param request validated Unix host session request.
     * @return SSH connection request.
     */
    private SshConnectionRequest toConnectionRequest(UnixHostSessionRequest request) {
        HostConfig host = request.host();
        UserConfig user = request.resolvedUser();

        return new SshConnectionRequest(
                host.host(),
                resolvePort(host),
                user.username(),
                user.password(),
                user.keyPath(),
                resolveConnectTimeout(host)
        );
    }

    /**
     * Resolves SSH port from host configuration.
     *
     * @param host host configuration.
     * @return configured port or default SSH port.
     */
    private Integer resolvePort(HostConfig host) {
        if (host.port() == null) {
            return DEFAULT_SSH_PORT;
        }

        return host.port();
    }

    /**
     * Resolves connection timeout from host command timeout.
     *
     * <p>The current generic model has command timeout but does not yet have a
     * dedicated SSH connect timeout. Reusing command timeout as a first
     * connection timeout approximation keeps the behavior configurable without
     * extending the model prematurely.</p>
     *
     * @param host host configuration.
     * @return connection timeout, or {@code null} when not configured.
     */
    private Duration resolveConnectTimeout(HostConfig host) {
        if (host.commandTimeoutSeconds() == null) {
            return null;
        }

        return Duration.ofSeconds(host.commandTimeoutSeconds());
    }
}