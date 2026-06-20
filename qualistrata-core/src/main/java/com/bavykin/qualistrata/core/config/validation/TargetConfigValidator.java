package com.bavykin.qualistrata.core.config.validation;

import com.bavykin.qualistrata.core.config.model.*;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;

import java.util.Map;

/**
 * Performs generic structural validation for target resource topology.
 *
 * <p>The validator checks only reusable framework-level structure. It does not
 * decide which concrete resource aliases are mandatory for a consumer project,
 * which target names are allowed, or whether a project-specific baseline should
 * run.</p>
 */
public class TargetConfigValidator {

    /** Validates a resolved target wrapper and its target topology. */
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

    /** Validates generic target topology. */
    public void validate(TargetConfig config) {
        if (config == null) {
            throw new FrameworkException("Target configuration must not be null.");
        }
        requireNotBlank(config.env(), "Target environment name must not be blank.");
        if (config.resources() == null) {
            throw new FrameworkException("Target configuration must contain resources section.");
        }

        validateUi(config.resources().ui());
        validateUnix(config.resources().unix());
        validateDatabases(config.resources().databases());
        validateApi(config.resources().api());
        validateMetrics(config.resources().metrics());
    }

    private void validateUi(Map<String, UiResourceConfig> resources) {
        if (resources == null) {
            return;
        }
        resources.forEach((alias, resource) -> {
            requireAlias(alias, "UI");
            if (resource == null) {
                throw new FrameworkException("UI resource configuration must not be null for alias: " + alias);
            }
            requireNotBlank(resource.baseUrl(), "UI base URL must not be blank for alias: " + alias);
        });
    }

    private void validateUnix(Map<String, UnixResourceConfig> resources) {
        if (resources == null) {
            return;
        }
        resources.forEach((alias, resource) -> {
            requireAlias(alias, "Unix");
            if (resource == null) {
                throw new FrameworkException("Unix resource configuration must not be null for alias: " + alias);
            }
            requireNotBlank(resource.host(), "Unix host address must not be blank for alias: " + alias);
            requirePositive(resource.port(), "Unix host port must be positive for alias: " + alias);
            requirePositive(resource.commandTimeoutSeconds(), "Unix command timeout must be positive for alias: " + alias);
            validateUnixPool(alias, resource.pool());
        });
    }

    private void validateDatabases(Map<String, DatabaseResourceConfig> resources) {
        if (resources == null) {
            return;
        }
        resources.forEach((alias, resource) -> {
            requireAlias(alias, "Database");
            if (resource == null) {
                throw new FrameworkException("Database resource configuration must not be null for alias: " + alias);
            }
            requireNotBlank(resource.type(), "Database type must not be blank for alias: " + alias);
            requirePositive(resource.port(), "Database port must be positive for alias: " + alias);
            requirePositive(resource.connectionTimeoutSeconds(), "Database connection timeout must be positive for alias: " + alias);
            requirePositive(resource.queryTimeoutSeconds(), "Database query timeout must be positive for alias: " + alias);
            validateDatabaseAddress(alias, resource);
            validateDatabasePool(alias, resource.pool());
        });
    }

    private void validateApi(Map<String, ApiResourceConfig> resources) {
        if (resources == null) {
            return;
        }
        resources.forEach((alias, resource) -> {
            requireAlias(alias, "API");
            if (resource == null) {
                throw new FrameworkException("API resource configuration must not be null for alias: " + alias);
            }
            requireNotBlank(resource.baseUrl(), "API base URL must not be blank for alias: " + alias);
        });
    }

    private void validateMetrics(Map<String, MetricsResourceConfig> resources) {
        if (resources == null) {
            return;
        }
        resources.forEach((alias, resource) -> {
            requireAlias(alias, "Metrics");
            if (resource == null) {
                throw new FrameworkException("Metrics resource configuration must not be null for alias: " + alias);
            }
            if (Boolean.TRUE.equals(resource.enabled())) {
                requireNotBlank(resource.type(), "Metrics type must not be blank for enabled alias: " + alias);
                requireNotBlank(resource.url(), "Metrics URL must not be blank for enabled alias: " + alias);
            }
        });
    }

    private void validateDatabaseAddress(String alias, DatabaseResourceConfig resource) {
        boolean hasJdbcUrl = !isBlank(resource.jdbcUrl());
        if (hasJdbcUrl) {
            return;
        }
        requireNotBlank(resource.host(), "Database host must not be blank for alias: " + alias + " when jdbcUrl is not configured.");
        requireNotBlank(resource.databaseName(), "Database name must not be blank for alias: " + alias + " when jdbcUrl is not configured.");
    }

    private void validateUnixPool(String alias, UnixPoolConfig pool) {
        if (pool == null) {
            return;
        }
        requirePositive(pool.size(), "Unix pool size must be positive for alias: " + alias);
        requireNonNegative(pool.keepAliveSeconds(), "Unix pool keep-alive seconds must be non-negative for alias: " + alias);
        requirePositive(pool.borrowTimeoutSeconds(), "Unix pool borrow timeout seconds must be positive for alias: " + alias);
    }

    private void validateDatabasePool(String alias, DatabasePoolConfig pool) {
        if (pool == null) {
            return;
        }
        requirePositive(pool.size(), "Database pool size must be positive for alias: " + alias);
        requirePositive(pool.borrowTimeoutSeconds(), "Database pool borrow timeout seconds must be positive for alias: " + alias);
        requireNonNegative(pool.idleTimeoutSeconds(), "Database pool idle timeout seconds must be non-negative for alias: " + alias);
    }

    private void requireAlias(String alias, String type) {
        requireNotBlank(alias, type + " resource alias must not be blank.");
    }

    private void requireNotBlank(String value, String message) {
        if (isBlank(value)) {
            throw new FrameworkException(message);
        }
    }

    private void requirePositive(Integer value, String message) {
        if (value != null && value <= 0) {
            throw new FrameworkException(message);
        }
    }

    private void requireNonNegative(Integer value, String message) {
        if (value != null && value < 0) {
            throw new FrameworkException(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
