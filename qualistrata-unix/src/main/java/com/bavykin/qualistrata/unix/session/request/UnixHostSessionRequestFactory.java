package com.bavykin.qualistrata.unix.session.request;

import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionException;

import java.util.Map;

/**
 * Factory for creating {@link UnixHostSessionRequest} instances from resolved
 * target configuration.
 *
 * <p>The factory hides host lookup logic from consuming projects. Consumers only
 * need to know the logical host alias and, optionally, the user alias to use for
 * the session.</p>
 *
 * <p>This class expects an already resolved and merged {@link TargetConfig}.
 * It does not load configuration files, merge secrets, or perform
 * project-specific validation.</p>
 */
public class UnixHostSessionRequestFactory {

    private final TargetConfig targetConfig;

    /**
     * Creates a Unix host session request factory.
     *
     * @param targetConfig resolved target configuration.
     */
    public UnixHostSessionRequestFactory(TargetConfig targetConfig) {
        if (targetConfig == null) {
            throw new UnixSessionException("Target configuration must not be null.");
        }

        this.targetConfig = targetConfig;
    }

    /**
     * Creates a Unix host session request for the given host alias using the
     * host default user.
     *
     * @param hostAlias logical host alias from target configuration.
     * @return Unix host session request.
     */
    public UnixHostSessionRequest create(String hostAlias) {
        return create(hostAlias, null);
    }

    /**
     * Creates a Unix host session request for the given host alias and user
     * alias.
     *
     * <p>When {@code userAlias} is {@code null} or blank, the host default user
     * will be used by {@link UnixHostSessionRequest}.</p>
     *
     * @param hostAlias logical host alias from target configuration.
     * @param userAlias optional user alias.
     * @return Unix host session request.
     */
    public UnixHostSessionRequest create(String hostAlias, String userAlias) {
        if (hostAlias == null || hostAlias.isBlank()) {
            throw new UnixSessionException("Unix host alias must not be blank.");
        }

        HostConfig host = resolveHost(hostAlias);

        return new UnixHostSessionRequest(
                hostAlias.trim(),
                host,
                normalizeUserAlias(userAlias)
        );
    }

    /**
     * Resolves a host configuration by logical alias.
     *
     * @param hostAlias logical host alias.
     * @return host configuration.
     */
    private HostConfig resolveHost(String hostAlias) {
        Map<String, HostConfig> hosts = targetConfig.hosts();

        if (hosts == null || hosts.isEmpty()) {
            throw new UnixSessionException("Target configuration does not contain Unix hosts.");
        }

        HostConfig host = hosts.get(hostAlias.trim());

        if (host == null) {
            throw new UnixSessionException("Unix host alias is not configured: " + hostAlias.trim());
        }

        return host;
    }

    /**
     * Normalizes optional user alias.
     *
     * @param userAlias optional user alias.
     * @return trimmed user alias, or {@code null} when blank.
     */
    private String normalizeUserAlias(String userAlias) {
        if (userAlias == null || userAlias.isBlank()) {
            return null;
        }

        return userAlias.trim();
    }
}