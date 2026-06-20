package com.bavykin.qualistrata.unix.session.request;

import com.bavykin.qualistrata.core.config.access.AccessResolver;
import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import com.bavykin.qualistrata.core.config.resolved.ResolvedUnixHostConfig;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionException;

import java.util.Map;

/**
 * Factory for creating {@link UnixHostSessionRequest} instances from the generic
 * QualiStrata resource/access model.
 *
 * <p>The factory resolves Unix resource topology and the selected access
 * profile through {@link AccessResolver}. It then adapts the resolved runtime
 * configuration to the lower-level Unix session request model used by the SSH
 * implementation.</p>
 */
public class UnixHostSessionRequestFactory {

    private final AccessResolver accessResolver;

    /**
     * Creates a Unix host session request factory from a resolved target.
     *
     * @param resolvedTarget resolved target containing target topology and secrets.
     */
    public UnixHostSessionRequestFactory(ResolvedTarget resolvedTarget) {
        if (resolvedTarget == null) {
            throw new UnixSessionException("Resolved target must not be null.");
        }
        this.accessResolver = new AccessResolver(resolvedTarget.config(), resolvedTarget.secrets());
    }

    /**
     * Creates a Unix host session request factory with an explicit access resolver.
     *
     * @param accessResolver resolver used to combine Unix resource topology and secrets.
     */
    public UnixHostSessionRequestFactory(AccessResolver accessResolver) {
        if (accessResolver == null) {
            throw new UnixSessionException("Access resolver must not be null.");
        }
        this.accessResolver = accessResolver;
    }

    /** Creates a Unix host session request using the resource default access profile. */
    public UnixHostSessionRequest create(String hostAlias) {
        return create(hostAlias, null);
    }

    /** Creates a Unix host session request using an explicit access profile when provided. */
    public UnixHostSessionRequest create(String hostAlias, String accessAlias) {
        if (hostAlias == null || hostAlias.isBlank()) {
            throw new UnixSessionException("Unix host alias must not be blank.");
        }

        try {
            ResolvedUnixHostConfig resolved = accessAlias == null || accessAlias.isBlank()
                    ? accessResolver.unix(hostAlias)
                    : accessResolver.unix(hostAlias, accessAlias);

            HostConfig host = toSessionHostConfig(resolved);

            return new UnixHostSessionRequest(
                    resolved.alias(),
                    host,
                    resolved.accessAlias()
            );
        } catch (FrameworkException exception) {
            throw new UnixSessionException(exception.getMessage(), exception);
        }
    }

    /** Converts resolved access config to the existing Unix session request model. */
    private HostConfig toSessionHostConfig(ResolvedUnixHostConfig resolved) {
        String password = resolved.password() == null ? null : resolved.password().value();

        UserConfig user = new UserConfig(
                resolved.username(),
                password,
                resolved.keyPath()
        );

        return new HostConfig(
                resolved.role(),
                resolved.host(),
                resolved.port(),
                resolved.accessAlias(),
                Map.of(resolved.accessAlias(), user),
                null,
                null,
                resolved.pool(),
                resolved.commandTimeoutSeconds()
        );
    }
}
