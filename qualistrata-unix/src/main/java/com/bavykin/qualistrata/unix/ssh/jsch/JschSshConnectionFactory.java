package com.bavykin.qualistrata.unix.ssh.jsch;

import com.bavykin.qualistrata.unix.ssh.*;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.nio.charset.StandardCharsets;

/**
 * JSch-based implementation of {@link SshConnectionFactory}.
 *
 * <p>The factory validates a generic {@link SshConnectionRequest}, configures
 * JSch authentication, opens a connected JSch {@link Session}, and wraps it as
 * {@link com.bavykin.qualistrata.unix.ssh.jsch.JschSshConnection}.</p>
 *
 * <p>The implementation intentionally stays behind the generic
 * {@link SshConnectionFactory} abstraction so that higher-level Unix session
 * code does not depend directly on JSch.</p>
 */
public class JschSshConnectionFactory implements SshConnectionFactory {

    private static final int DEFAULT_SSH_PORT = 22;
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 30_000;

    private final JSch jsch;
    private final SshConnectionRequestValidator requestValidator;

    /**
     * Creates a JSch SSH connection factory.
     */
    public JschSshConnectionFactory() {
        this(new JSch(), new SshConnectionRequestValidator());
    }

    /**
     * Creates a JSch SSH connection factory with explicitly provided
     * collaborators.
     *
     * @param jsch JSch instance.
     * @param requestValidator SSH connection request validator.
     */
    JschSshConnectionFactory(
            JSch jsch,
            SshConnectionRequestValidator requestValidator
    ) {
        if (jsch == null) {
            throw new SshConnectionException("JSch instance must not be null.");
        }

        if (requestValidator == null) {
            throw new SshConnectionException("SSH connection request validator must not be null.");
        }

        this.jsch = jsch;
        this.requestValidator = requestValidator;
    }

    /**
     * Opens a new JSch-backed SSH connection.
     *
     * @param request SSH connection request.
     * @return opened SSH connection.
     */
    @Override
    public SshConnection open(SshConnectionRequest request) {
        requestValidator.validate(request);

        try {
            configureIdentity(request);

            Session session = jsch.getSession(
                    request.username(),
                    request.host(),
                    resolvePort(request)
            );

            configureSession(session, request);
            session.connect(resolveConnectTimeoutMillis(request));

            return new com.bavykin.qualistrata.unix.ssh.jsch.JschSshConnection(session);
        } catch (JSchException exception) {
            throw new SshConnectionException(
                    "Failed to open SSH connection to "
                            + request.username()
                            + "@"
                            + request.host()
                            + ":"
                            + resolvePort(request),
                    exception
            );
        }
    }

    /**
     * Configures key-based authentication when key path is provided.
     *
     * @param request SSH connection request.
     * @throws JSchException if identity cannot be added.
     */
    private void configureIdentity(SshConnectionRequest request) throws JSchException {
        if (request.keyPath() == null || request.keyPath().trim().isEmpty()) {
            return;
        }

        jsch.addIdentity(request.keyPath());
    }

    /**
     * Configures a JSch session.
     *
     * @param session JSch session.
     * @param request SSH connection request.
     */
    private void configureSession(Session session, SshConnectionRequest request) {
        if (request.password() != null && !request.password().isBlank()) {
            session.setPassword(request.password().getBytes(StandardCharsets.UTF_8));
        }

        /*
         * Strict host key checking is intentionally disabled for the initial
         * generic implementation because QualiStrata does not yet model known
         * hosts files or host key policies. This should become configurable
         * before production-grade SSH hardening.
         */
        session.setConfig("StrictHostKeyChecking", "no");
    }

    /**
     * Resolves SSH port.
     *
     * @param request SSH connection request.
     * @return configured port or default SSH port.
     */
    private int resolvePort(SshConnectionRequest request) {
        if (request.port() == null) {
            return DEFAULT_SSH_PORT;
        }

        return request.port();
    }

    /**
     * Resolves connection timeout in milliseconds.
     *
     * @param request SSH connection request.
     * @return configured or default timeout in milliseconds.
     */
    private int resolveConnectTimeoutMillis(SshConnectionRequest request) {
        if (request.connectTimeout() == null) {
            return DEFAULT_CONNECT_TIMEOUT_MILLIS;
        }

        return Math.toIntExact(request.connectTimeout().toMillis());
    }
}