package com.bavykin.qualistrata.unix.session;

import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;

import java.util.Map;

/**
 * Validates Unix host session requests.
 *
 * <p>The validator checks only generic session-level rules. It does not verify
 * network reachability, credentials validity, SSH protocol support, shell
 * availability, or command execution behavior.</p>
 */
public class UnixHostSessionRequestValidator {

    /**
     * Creates a Unix host session request validator.
     *
     * <p>The validator is stateless and can be reused by direct and pooled
     * session provider implementations.</p>
     */
    public UnixHostSessionRequestValidator() {
    }

    /**
     * Validates a Unix host session request.
     *
     * @param request session request to validate.
     * @throws UnixSessionException if the request is structurally invalid.
     */
    public void validate(UnixHostSessionRequest request) {
        if (request == null) {
            throw new UnixSessionException("Unix host session request must not be null.");
        }

        requireNotBlank(request.hostAlias(), "Unix host alias must not be blank.");

        HostConfig host = request.host();

        if (host == null) {
            throw new UnixSessionException(
                    "Unix host configuration must not be null for alias: " + request.hostAlias()
            );
        }

        requireNotBlank(
                host.host(),
                "Unix host address must not be blank for alias: " + request.hostAlias()
        );

        requirePositive(
                host.port(),
                "Unix host port must be positive for alias: " + request.hostAlias()
        );

        validateUser(request);
        validatePool(request.hostAlias(), host.pool());
    }

    /**
     * Validates the effective user selected for the request.
     *
     * @param request session request.
     */
    private void validateUser(UnixHostSessionRequest request) {
        String userAlias = request.resolvedUserAlias();

        requireNotBlank(
                userAlias,
                "Unix user alias must be provided or default user must be configured for host alias: "
                        + request.hostAlias()
        );

        Map<String, UserConfig> users = request.host().users();

        if (users == null || users.isEmpty()) {
            throw new UnixSessionException(
                    "Unix users must be configured for host alias: " + request.hostAlias()
            );
        }

        UserConfig user = users.get(userAlias);

        if (user == null) {
            throw new UnixSessionException(
                    "Unix user '"
                            + userAlias
                            + "' is not defined for host alias: "
                            + request.hostAlias()
            );
        }

        requireNotBlank(
                user.username(),
                "Unix username must not be blank for user alias: "
                        + userAlias
                        + " on host alias: "
                        + request.hostAlias()
        );
    }

    /**
     * Validates optional Unix session pool configuration.
     *
     * @param hostAlias logical host alias.
     * @param pool pool configuration.
     */
    private void validatePool(String hostAlias, UnixPoolConfig pool) {
        if (pool == null) {
            return;
        }

        requirePositive(pool.size(), "Unix session pool size must be positive for host alias: " + hostAlias);
        requireNonNegative(
                pool.keepAliveSeconds(),
                "Unix session pool keep-alive seconds must be non-negative for host alias: " + hostAlias
        );
        requirePositive(
                pool.borrowTimeoutSeconds(),
                "Unix session pool borrow timeout seconds must be positive for host alias: " + hostAlias
        );
    }

    /**
     * Requires a string value to be non-null and non-blank.
     *
     * @param value value to check.
     * @param message exception message.
     */
    private void requireNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new UnixSessionException(message);
        }
    }

    /**
     * Requires an optional integer value to be positive when present.
     *
     * @param value value to check.
     * @param message exception message.
     */
    private void requirePositive(Integer value, String message) {
        if (value != null && value <= 0) {
            throw new UnixSessionException(message);
        }
    }

    /**
     * Requires an optional integer value to be non-negative when present.
     *
     * @param value value to check.
     * @param message exception message.
     */
    private void requireNonNegative(Integer value, String message) {
        if (value != null && value < 0) {
            throw new UnixSessionException(message);
        }
    }
}