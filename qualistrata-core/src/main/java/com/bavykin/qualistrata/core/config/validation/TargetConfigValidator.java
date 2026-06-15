package com.bavykin.qualistrata.core.config.validation;

import com.bavykin.qualistrata.core.config.model.DBConfig;
import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.config.model.UIConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;

import java.util.Map;

/**
 * Performs generic structural validation for target configuration.
 *
 * <p>The validator checks only reusable framework-level rules. It does not
 * decide which application-specific sections are mandatory, which environment
 * names are allowed, which hosts must exist, or whether credentials are
 * complete for a concrete project.</p>
 *
 * <p>Product-specific validation must be implemented by consuming projects or
 * project-specific integration modules.</p>
 */
public class TargetConfigValidator {

    /**
     * Creates a target configuration validator.
     *
     * <p>The validator is stateless. A single instance can be reused across
     * multiple configuration resolution operations.</p>
     */
    public TargetConfigValidator() {
    }

    /**
     * Validates a resolved target.
     *
     * @param resolvedTarget resolved target to validate.
     * @throws FrameworkException if the resolved target is structurally invalid.
     */
    public void validate(ResolvedTarget resolvedTarget) {
        if (resolvedTarget == null) {
            throw new FrameworkException("Resolved target must not be null.");
        }

        requireNotBlank(resolvedTarget.name(), "Resolved target name must not be blank.");

        if (resolvedTarget.config() == null) {
            throw new FrameworkException("Resolved target configuration must not be null.");
        }

        validate(resolvedTarget.config());
    }

    /**
     * Validates generic target configuration structure.
     *
     * @param config target configuration to validate.
     * @throws FrameworkException if the configuration is structurally invalid.
     */
    public void validate(TargetConfig config) {
        if (config == null) {
            throw new FrameworkException("Target configuration must not be null.");
        }

        requireNotBlank(config.env(), "Target environment name must not be blank.");

        validateUi(config.ui());
        validateDb(config.db());
        validateHosts(config.hosts());
    }

    /**
     * Validates the optional UI section.
     *
     * <p>The UI section itself is not mandatory at core level. However, when it
     * is present, its base URL must not be blank.</p>
     *
     * @param ui UI configuration section.
     */
    private void validateUi(UIConfig ui) {
        if (ui == null) {
            return;
        }

        requireNotBlank(ui.baseUrl(), "UI base URL must not be blank when UI section is configured.");
    }

    /**
     * Validates the optional database section.
     *
     * <p>The database section itself is not mandatory at core level. However,
     * when a database port is provided, it must be positive.</p>
     *
     * @param db database configuration section.
     */
    private void validateDb(DBConfig db) {
        if (db == null) {
            return;
        }

        requirePositive(db.port(), "Database port must be positive when configured.");
    }

    /**
     * Validates optional host definitions.
     *
     * @param hosts map of host aliases to host configurations.
     */
    private void validateHosts(Map<String, HostConfig> hosts) {
        if (hosts == null) {
            return;
        }

        hosts.forEach(this::validateHost);
    }

    /**
     * Validates a single host definition.
     *
     * @param alias logical host alias.
     * @param host host configuration.
     */
    private void validateHost(String alias, HostConfig host) {
        requireNotBlank(alias, "Host alias must not be blank.");

        if (host == null) {
            throw new FrameworkException("Host configuration must not be null for alias: " + alias);
        }

        requireNotBlank(host.host(), "Host address must not be blank for alias: " + alias);
        requirePositive(host.port(), "Host port must be positive for alias: " + alias);

        validateUsers(alias, host.users());
        validateDefaultUser(alias, host.defaultUser(), host.users());
        validatePool(alias, host.pool());
        requirePositive(host.commandTimeoutSeconds(), "Command timeout must be positive for alias: " + alias);
    }

    /**
     * Validates optional user definitions for a host.
     *
     * @param hostAlias logical host alias.
     * @param users map of user aliases to user configurations.
     */
    private void validateUsers(String hostAlias, Map<String, UserConfig> users) {
        if (users == null) {
            return;
        }

        users.forEach((userAlias, user) -> validateUser(hostAlias, userAlias, user));
    }

    /**
     * Validates a single user definition.
     *
     * @param hostAlias logical host alias.
     * @param userAlias logical user alias.
     * @param user user configuration.
     */
    private void validateUser(String hostAlias, String userAlias, UserConfig user) {
        requireNotBlank(userAlias, "User alias must not be blank for host alias: " + hostAlias);

        if (user == null) {
            throw new FrameworkException(
                    "User configuration must not be null for user alias: "
                            + userAlias
                            + " on host alias: "
                            + hostAlias
            );
        }

        requireNotBlank(
                user.username(),
                "Username must not be blank for user alias: "
                        + userAlias
                        + " on host alias: "
                        + hostAlias
        );
    }

    /**
     * Validates that the configured default user points to an existing user
     * definition when users are provided.
     *
     * @param hostAlias logical host alias.
     * @param defaultUser configured default user alias.
     * @param users map of user aliases to user configurations.
     */
    private void validateDefaultUser(
            String hostAlias,
            String defaultUser,
            Map<String, UserConfig> users
    ) {
        if (isBlank(defaultUser) || users == null || users.isEmpty()) {
            return;
        }

        if (!users.containsKey(defaultUser)) {
            throw new FrameworkException(
                    "Default user '"
                            + defaultUser
                            + "' is not defined for host alias: "
                            + hostAlias
            );
        }
    }

    /**
     * Validates optional Unix connection pool configuration.
     *
     * @param hostAlias logical host alias.
     * @param pool Unix connection pool configuration.
     */
    private void validatePool(String hostAlias, UnixPoolConfig pool) {
        if (pool == null) {
            return;
        }

        requirePositive(pool.size(), "Unix pool size must be positive for host alias: " + hostAlias);
        requireNonNegative(
                pool.keepAliveSeconds(),
                "Unix pool keep-alive seconds must be non-negative for host alias: " + hostAlias
        );
        requirePositive(
                pool.borrowTimeoutSeconds(),
                "Unix pool borrow timeout seconds must be positive for host alias: " + hostAlias
        );
    }

    /**
     * Requires a string value to be non-null and non-blank.
     *
     * @param value value to check.
     * @param message exception message.
     */
    private void requireNotBlank(String value, String message) {
        if (isBlank(value)) {
            throw new FrameworkException(message);
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
            throw new FrameworkException(message);
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
            throw new FrameworkException(message);
        }
    }

    /**
     * Checks whether a string is null, empty, or contains only whitespace.
     *
     * @param value value to check.
     * @return {@code true} when the value is null or blank.
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}