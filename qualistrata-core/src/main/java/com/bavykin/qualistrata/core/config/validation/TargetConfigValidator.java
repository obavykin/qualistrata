package com.bavykin.qualistrata.core.config.validation;

import com.bavykin.qualistrata.core.config.model.ApiResourceConfig;
import com.bavykin.qualistrata.core.config.model.DatabasePoolConfig;
import com.bavykin.qualistrata.core.config.model.DatabaseResourceConfig;
import com.bavykin.qualistrata.core.config.model.MetricsResourceConfig;
import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.model.ResourcesConfig;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.config.model.UiResourceConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UnixResourceConfig;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;

import java.util.Map;

/**
 * Performs generic structural validation for target configuration.
 *
 * <p>The validator checks only reusable framework-level rules. It does not
 * decide which application-specific sections are mandatory, which environment
 * names are allowed, which resources must exist, or whether credentials are
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
        requireNotBlankWhenPresent(config.release(), "Target release identifier must not be blank when configured.");

        validateResources(config.resources());
    }

    /**
     * Validates the optional resources section.
     *
     * <p>The resources section itself is not mandatory at core level. A
     * consuming project may define a minimal target and apply stricter
     * project-specific rules later. When resources are present, each configured
     * resource category must be structurally valid.</p>
     *
     * @param resources generic resource topology.
     */
    private void validateResources(ResourcesConfig resources) {
        if (resources == null) {
            return;
        }

        validateUiResources(resources.ui());
        validateUnixResources(resources.unix());
        validateDatabaseResources(resources.databases());
        validateApiResources(resources.api());
        validateMetricsResources(resources.metrics());
    }

    /**
     * Validates configured UI resources.
     */
    private void validateUiResources(Map<String, UiResourceConfig> resources) {
        if (resources == null) {
            return;
        }

        resources.forEach(this::validateUiResource);
    }

    /**
     * Validates one UI resource.
     */
    private void validateUiResource(String alias, UiResourceConfig resource) {
        requireNotBlank(alias, "UI resource alias must not be blank.");

        if (resource == null) {
            throw new FrameworkException("UI resource configuration must not be null for alias: " + alias);
        }

        requireNotBlank(resource.baseUrl(), "UI base URL must not be blank for resource alias: " + alias);
        requireNotBlankWhenPresent(resource.defaultAccess(),
                "UI default access alias must not be blank for resource alias: " + alias);
    }

    /**
     * Validates configured Unix resources.
     */
    private void validateUnixResources(Map<String, UnixResourceConfig> resources) {
        if (resources == null) {
            return;
        }

        resources.forEach(this::validateUnixResource);
    }

    /**
     * Validates one Unix resource.
     */
    private void validateUnixResource(String alias, UnixResourceConfig resource) {
        requireNotBlank(alias, "Unix resource alias must not be blank.");

        if (resource == null) {
            throw new FrameworkException("Unix resource configuration must not be null for alias: " + alias);
        }

        requireNotBlank(resource.host(), "Unix host address must not be blank for resource alias: " + alias);
        requirePositive(resource.port(), "Unix host port must be positive for resource alias: " + alias);
        requireNotBlankWhenPresent(resource.defaultAccess(),
                "Unix default access alias must not be blank for resource alias: " + alias);
        requirePositive(resource.commandTimeoutSeconds(),
                "Unix command timeout must be positive for resource alias: " + alias);
        validateUnixPool(alias, resource.pool());
    }

    /**
     * Validates configured database resources.
     */
    private void validateDatabaseResources(Map<String, DatabaseResourceConfig> resources) {
        if (resources == null) {
            return;
        }

        resources.forEach(this::validateDatabaseResource);
    }

    /**
     * Validates one database resource.
     */
    private void validateDatabaseResource(String alias, DatabaseResourceConfig resource) {
        requireNotBlank(alias, "Database resource alias must not be blank.");

        if (resource == null) {
            throw new FrameworkException("Database resource configuration must not be null for alias: " + alias);
        }

        requireNotBlankWhenPresent(resource.type(), "Database type must not be blank for resource alias: " + alias);
        requireNotBlankWhenPresent(resource.host(), "Database host must not be blank for resource alias: " + alias);
        requirePositive(resource.port(), "Database port must be positive for resource alias: " + alias);
        requireNotBlankWhenPresent(resource.databaseName(),
                "Database name must not be blank for resource alias: " + alias);
        requireNotBlankWhenPresent(resource.jdbcUrl(), "JDBC URL must not be blank for resource alias: " + alias);
        requireNotBlankWhenPresent(resource.defaultAccess(),
                "Database default access alias must not be blank for resource alias: " + alias);
        requirePositive(resource.connectionTimeoutSeconds(),
                "Database connection timeout must be positive for resource alias: " + alias);
        requirePositive(resource.queryTimeoutSeconds(),
                "Database query timeout must be positive for resource alias: " + alias);
        validateDatabasePool(alias, resource.pool());
    }

    /**
     * Validates configured API resources.
     */
    private void validateApiResources(Map<String, ApiResourceConfig> resources) {
        if (resources == null) {
            return;
        }

        resources.forEach(this::validateApiResource);
    }

    /**
     * Validates one API resource.
     */
    private void validateApiResource(String alias, ApiResourceConfig resource) {
        requireNotBlank(alias, "API resource alias must not be blank.");

        if (resource == null) {
            throw new FrameworkException("API resource configuration must not be null for alias: " + alias);
        }

        requireNotBlankWhenPresent(resource.baseUrl(), "API base URL must not be blank for resource alias: " + alias);
        requireNotBlankWhenPresent(resource.defaultAccess(),
                "API default access alias must not be blank for resource alias: " + alias);
    }

    /**
     * Validates configured metrics resources.
     */
    private void validateMetricsResources(Map<String, MetricsResourceConfig> resources) {
        if (resources == null) {
            return;
        }

        resources.forEach(this::validateMetricsResource);
    }

    /**
     * Validates one metrics resource.
     */
    private void validateMetricsResource(String alias, MetricsResourceConfig resource) {
        requireNotBlank(alias, "Metrics resource alias must not be blank.");

        if (resource == null) {
            throw new FrameworkException("Metrics resource configuration must not be null for alias: " + alias);
        }

        requireNotBlankWhenPresent(resource.url(), "Metrics URL must not be blank for resource alias: " + alias);
        requireNotBlankWhenPresent(resource.defaultAccess(),
                "Metrics default access alias must not be blank for resource alias: " + alias);
    }

    /**
     * Validates optional Unix connection pool configuration.
     */
    private void validateUnixPool(String resourceAlias, UnixPoolConfig pool) {
        if (pool == null) {
            return;
        }

        requirePositive(pool.size(), "Unix pool size must be positive for resource alias: " + resourceAlias);
        requireNonNegative(pool.keepAliveSeconds(),
                "Unix pool keep-alive seconds must be non-negative for resource alias: " + resourceAlias);
        requirePositive(pool.borrowTimeoutSeconds(),
                "Unix pool borrow timeout seconds must be positive for resource alias: " + resourceAlias);
    }

    /**
     * Validates optional database connection pool configuration.
     */
    private void validateDatabasePool(String resourceAlias, DatabasePoolConfig pool) {
        if (pool == null) {
            return;
        }

        requirePositive(pool.size(), "Database pool size must be positive for resource alias: " + resourceAlias);
        requirePositive(pool.borrowTimeoutSeconds(),
                "Database pool borrow timeout seconds must be positive for resource alias: " + resourceAlias);
        requirePositive(pool.idleTimeoutSeconds(),
                "Database pool idle timeout seconds must be positive for resource alias: " + resourceAlias);
    }

    /**
     * Requires a string value to be non-null and non-blank.
     */
    private void requireNotBlank(String value, String message) {
        if (isBlank(value)) {
            throw new FrameworkException(message);
        }
    }

    /**
     * Requires a string value to be non-blank when present.
     */
    private void requireNotBlankWhenPresent(String value, String message) {
        if (value != null && value.trim().isEmpty()) {
            throw new FrameworkException(message);
        }
    }

    /**
     * Requires an integer value to be positive when present.
     */
    private void requirePositive(Integer value, String message) {
        if (value != null && value <= 0) {
            throw new FrameworkException(message);
        }
    }

    /**
     * Requires an integer value to be non-negative when present.
     */
    private void requireNonNegative(Integer value, String message) {
        if (value != null && value < 0) {
            throw new FrameworkException(message);
        }
    }

    /**
     * Checks whether text is null or blank.
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
