package com.bavykin.qualistrata.unix.session;

import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;

/**
 * Request for opening or borrowing a Unix host session.
 *
 * <p>The request contains a logical host alias, resolved host configuration,
 * and an optional logical user alias. If the user alias is not provided, the
 * session layer may use the host's default user.</p>
 *
 * <p>The request does not decide whether a new physical connection should be
 * opened or an existing pooled connection should be reused. That decision
 * belongs to the {@link UnixSessionProvider} implementation.</p>
 *
 * @param hostAlias logical host alias from target configuration.
 * @param host host configuration.
 * @param userAlias optional logical user alias. When blank or null, the host
 *                  default user may be used.
 */
public record UnixHostSessionRequest(
        String hostAlias,
        HostConfig host,
        String userAlias
) {

    /**
     * Resolves the effective user alias for this request.
     *
     * <p>The explicitly provided user alias has priority. If it is blank or
     * missing, the host default user is used when available.</p>
     *
     * @return effective user alias, or {@code null} when it cannot be resolved.
     */
    public String resolvedUserAlias() {
        if (!isBlank(userAlias)) {
            return userAlias.trim();
        }

        if (host == null || isBlank(host.defaultUser())) {
            return null;
        }

        return host.defaultUser().trim();
    }

    /**
     * Resolves the effective user configuration for this request.
     *
     * @return effective user configuration, or {@code null} when it cannot be
     * resolved.
     */
    public UserConfig resolvedUser() {
        String resolvedAlias = resolvedUserAlias();

        if (host == null || host.users() == null || resolvedAlias == null) {
            return null;
        }

        return host.users().get(resolvedAlias);
    }

    /**
     * Checks whether a string is null, empty, or contains only whitespace.
     *
     * @param value value to check.
     * @return {@code true} when value is null or blank.
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}