package com.bavykin.qualistrata.core.config.access;

import com.bavykin.qualistrata.core.config.model.*;
import com.bavykin.qualistrata.core.config.resolved.*;
import com.bavykin.qualistrata.core.config.secrets.*;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;

import java.util.Map;

/**
 * Resolves runtime access configuration from target resources and secrets.
 *
 * <p>This resolver is intentionally consumer-project-agnostic. Projects choose
 * resource aliases and access aliases, while core only applies the generic
 * resolution rule:</p>
 *
 * <pre>
 * resources.&lt;type&gt;.&lt;resource-alias&gt; + access.&lt;type&gt;.&lt;resource-alias&gt;.&lt;access-alias&gt;
 * </pre>
 *
 * <p>When an explicit access alias is not provided, the resource's
 * {@code defaultAccess} value is used. This makes simple projects concise while
 * still allowing tests to request another identity explicitly.</p>
 */
public class AccessResolver {

    private final TargetConfig target;
    private final SecretsConfig secrets;

    /**
     * Creates an access resolver for a resolved target and optional secrets.
     *
     * @param target non-secret resource topology.
     * @param secrets secret access profiles, or {@code null} when not needed.
     */
    public AccessResolver(TargetConfig target, SecretsConfig secrets) {
        if (target == null) {
            throw new FrameworkException("Target configuration must not be null for access resolution.");
        }
        this.target = target;
        this.secrets = secrets;
    }

    /** Resolves Unix access using the resource default access profile. */
    public ResolvedUnixHostConfig unix(String alias) {
        UnixResourceConfig resource = unixResource(alias);
        return unix(alias, resource.defaultAccess());
    }

    /** Resolves Unix access using an explicit access profile alias. */
    public ResolvedUnixHostConfig unix(String alias, String accessAlias) {
        UnixResourceConfig resource = unixResource(alias);
        String resolvedAccessAlias = requireAccessAlias(alias, accessAlias, resource.defaultAccess(), "Unix");
        UnixAccessSecret access = requireAccess(
                access().unix(), alias, resolvedAccessAlias, "Unix"
        );

        requireText(access.username(), "Unix username must not be blank for resource alias '" + alias
                + "' and access alias '" + resolvedAccessAlias + "'.");

        return new ResolvedUnixHostConfig(
                alias.trim(),
                resolvedAccessAlias,
                resource,
                access.username(),
                secret(access.password()),
                access.keyPath(),
                secret(access.keyPassphrase())
        );
    }

    /** Resolves database access using the resource default access profile. */
    public ResolvedDatabaseConfig database(String alias) {
        DatabaseResourceConfig resource = databaseResource(alias);
        return database(alias, resource.defaultAccess());
    }

    /** Resolves database access using an explicit access profile alias. */
    public ResolvedDatabaseConfig database(String alias, String accessAlias) {
        DatabaseResourceConfig resource = databaseResource(alias);
        String resolvedAccessAlias = requireAccessAlias(alias, accessAlias, resource.defaultAccess(), "Database");
        DatabaseAccessSecret access = requireAccess(
                access().databases(), alias, resolvedAccessAlias, "Database"
        );

        requireText(access.username(), "Database username must not be blank for resource alias '" + alias
                + "' and access alias '" + resolvedAccessAlias + "'.");

        return new ResolvedDatabaseConfig(
                alias.trim(),
                resolvedAccessAlias,
                resource,
                access.username(),
                secret(access.password()),
                secret(access.token())
        );
    }

    /** Resolves UI access using the resource default access profile. */
    public ResolvedUiConfig ui(String alias) {
        UiResourceConfig resource = uiResource(alias);
        return ui(alias, resource.defaultAccess());
    }

    /** Resolves UI access using an explicit access profile alias. */
    public ResolvedUiConfig ui(String alias, String accessAlias) {
        UiResourceConfig resource = uiResource(alias);
        String resolvedAccessAlias = requireAccessAlias(alias, accessAlias, resource.defaultAccess(), "UI");
        UiAccessSecret access = requireAccess(access().ui(), alias, resolvedAccessAlias, "UI");

        return new ResolvedUiConfig(
                alias.trim(),
                resolvedAccessAlias,
                resource,
                access.username(),
                secret(access.password()),
                secret(access.token())
        );
    }

    /** Resolves metrics access using the resource default access profile. */
    public ResolvedMetricsConfig metrics(String alias) {
        MetricsResourceConfig resource = metricsResource(alias);
        return metrics(alias, resource.defaultAccess());
    }

    /** Resolves metrics access using an explicit access profile alias. */
    public ResolvedMetricsConfig metrics(String alias, String accessAlias) {
        MetricsResourceConfig resource = metricsResource(alias);
        String resolvedAccessAlias = requireAccessAlias(alias, accessAlias, resource.defaultAccess(), "Metrics");
        MetricsAccessSecret access = requireAccess(access().metrics(), alias, resolvedAccessAlias, "Metrics");

        return new ResolvedMetricsConfig(
                alias.trim(),
                resolvedAccessAlias,
                resource,
                access.username(),
                secret(access.password()),
                secret(access.token())
        );
    }

    private UiResourceConfig uiResource(String alias) {
        return requireResource(resources().ui(), alias, "UI");
    }

    private UnixResourceConfig unixResource(String alias) {
        return requireResource(resources().unix(), alias, "Unix");
    }

    private DatabaseResourceConfig databaseResource(String alias) {
        return requireResource(resources().databases(), alias, "Database");
    }

    private MetricsResourceConfig metricsResource(String alias) {
        return requireResource(resources().metrics(), alias, "Metrics");
    }

    private ResourcesConfig resources() {
        if (target.resources() == null) {
            throw new FrameworkException("Target configuration must contain resources section.");
        }
        return target.resources();
    }

    private AccessSecretsConfig access() {
        if (secrets == null || secrets.access() == null) {
            throw new FrameworkException("Secrets configuration must contain access section for access resolution.");
        }
        return secrets.access();
    }

    private <T> T requireResource(Map<String, T> resources, String alias, String type) {
        requireText(alias, type + " resource alias must not be blank.");
        if (resources == null || resources.isEmpty()) {
            throw new FrameworkException("Target configuration does not contain " + type + " resources.");
        }
        T resource = resources.get(alias.trim());
        if (resource == null) {
            throw new FrameworkException(type + " resource alias is not configured: " + alias.trim());
        }
        return resource;
    }

    private <T> T requireAccess(
            Map<String, Map<String, T>> accessByResource,
            String resourceAlias,
            String accessAlias,
            String type
    ) {
        if (accessByResource == null || accessByResource.isEmpty()) {
            throw new FrameworkException("Secrets configuration does not contain " + type + " access profiles.");
        }
        Map<String, T> profiles = accessByResource.get(resourceAlias.trim());
        if (profiles == null || profiles.isEmpty()) {
            throw new FrameworkException(type + " access profiles are not configured for resource alias: "
                    + resourceAlias.trim());
        }
        T access = profiles.get(accessAlias);
        if (access == null) {
            throw new FrameworkException(type + " access alias '" + accessAlias
                    + "' is not configured for resource alias: " + resourceAlias.trim());
        }
        return access;
    }

    private String requireAccessAlias(
            String resourceAlias,
            String explicitAccessAlias,
            String defaultAccessAlias,
            String type
    ) {
        String value = isBlank(explicitAccessAlias) ? defaultAccessAlias : explicitAccessAlias;
        requireText(value, type + " access alias must be provided explicitly or defaultAccess must be configured for resource alias: "
                + resourceAlias);
        return value.trim();
    }

    private SecretValue secret(String value) {
        if (isBlank(value)) {
            return null;
        }
        return new SecretValue(value.trim());
    }

    private void requireText(String value, String message) {
        if (isBlank(value)) {
            throw new FrameworkException(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
