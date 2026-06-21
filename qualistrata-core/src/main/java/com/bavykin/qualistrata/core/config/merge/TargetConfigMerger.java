package com.bavykin.qualistrata.core.config.merge;

import com.bavykin.qualistrata.core.config.model.ApiResourceConfig;
import com.bavykin.qualistrata.core.config.model.DatabasePoolConfig;
import com.bavykin.qualistrata.core.config.model.DatabaseResourceConfig;
import com.bavykin.qualistrata.core.config.model.MetricsResourceConfig;
import com.bavykin.qualistrata.core.config.model.ResourcesConfig;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.config.model.UiResourceConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UnixResourceConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Merges layered target configuration objects into one effective configuration.
 *
 * <p>QualiStrata separates committed target configuration from runtime secret
 * and override layers. For example, a public target file may define resource
 * hostnames, ports, and access aliases, while a local uncommitted secrets file
 * may provide credentials through the secrets model. This merger combines
 * non-secret target layers without knowing where they came from.</p>
 *
 * <p>The merge rule is intentionally simple and deterministic: override values
 * win only when they are explicitly non-null. Null values in the override layer
 * do not erase values from the base layer.</p>
 *
 * <p>This class performs only in-memory merging. It does not load files, resolve
 * target names, read system properties, resolve secrets, or apply
 * project-specific defaults.</p>
 */
public class TargetConfigMerger {

    /**
     * Creates a target configuration merger.
     *
     * <p>The merger is stateless. A single instance can be reused across
     * multiple configuration resolution operations.</p>
     */
    public TargetConfigMerger() {
    }

    /**
     * Merges a base target configuration with an override target configuration.
     *
     * <p>The returned configuration represents the effective target state after
     * applying the override layer on top of the base layer.</p>
     *
     * @param base base configuration layer.
     * @param override overriding configuration layer.
     * @return merged configuration, or the non-null argument when the other
     *         argument is null.
     */
    public TargetConfig merge(TargetConfig base, TargetConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new TargetConfig(
                valueOrBase(override.env(), base.env()),
                valueOrBase(override.release(), base.release()),
                mergeResources(base.resources(), override.resources())
        );
    }

    /**
     * Merges the generic resources section.
     *
     * <p>Each resource category is merged independently by resource alias so an
     * override file can change only one database, host, UI, API, or metrics
     * resource without repeating the whole target topology.</p>
     *
     * @param base base resources section.
     * @param override overriding resources section.
     * @return merged resources section.
     */
    private ResourcesConfig mergeResources(ResourcesConfig base, ResourcesConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new ResourcesConfig(
                mergeMap(base.ui(), override.ui(), this::mergeUiResource),
                mergeMap(base.unix(), override.unix(), this::mergeUnixResource),
                mergeMap(base.databases(), override.databases(), this::mergeDatabaseResource),
                mergeMap(base.api(), override.api(), this::mergeApiResource),
                mergeMap(base.metrics(), override.metrics(), this::mergeMetricsResource)
        );
    }

    /**
     * Merges UI resource maps by alias.
     */
    private UiResourceConfig mergeUiResource(UiResourceConfig base, UiResourceConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new UiResourceConfig(
                valueOrBase(override.type(), base.type()),
                valueOrBase(override.baseUrl(), base.baseUrl()),
                valueOrBase(override.defaultAccess(), base.defaultAccess())
        );
    }

    /**
     * Merges Unix resource maps by alias.
     */
    private UnixResourceConfig mergeUnixResource(UnixResourceConfig base, UnixResourceConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new UnixResourceConfig(
                valueOrBase(override.role(), base.role()),
                valueOrBase(override.host(), base.host()),
                valueOrBase(override.port(), base.port()),
                valueOrBase(override.defaultAccess(), base.defaultAccess()),
                valueOrBase(override.commandTimeoutSeconds(), base.commandTimeoutSeconds()),
                mergeUnixPool(base.pool(), override.pool())
        );
    }

    /**
     * Merges database resource maps by alias.
     */
    private DatabaseResourceConfig mergeDatabaseResource(
            DatabaseResourceConfig base,
            DatabaseResourceConfig override
    ) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new DatabaseResourceConfig(
                valueOrBase(override.type(), base.type()),
                valueOrBase(override.host(), base.host()),
                valueOrBase(override.port(), base.port()),
                valueOrBase(override.databaseName(), base.databaseName()),
                valueOrBase(override.schema(), base.schema()),
                valueOrBase(override.jdbcUrl(), base.jdbcUrl()),
                valueOrBase(override.connectionMode(), base.connectionMode()),
                valueOrBase(override.defaultAccess(), base.defaultAccess()),
                valueOrBase(override.connectionTimeoutSeconds(), base.connectionTimeoutSeconds()),
                valueOrBase(override.queryTimeoutSeconds(), base.queryTimeoutSeconds()),
                mergeDatabasePool(base.pool(), override.pool())
        );
    }

    /**
     * Merges API resource maps by alias.
     */
    private ApiResourceConfig mergeApiResource(ApiResourceConfig base, ApiResourceConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new ApiResourceConfig(
                valueOrBase(override.type(), base.type()),
                valueOrBase(override.baseUrl(), base.baseUrl()),
                valueOrBase(override.defaultAccess(), base.defaultAccess())
        );
    }

    /**
     * Merges metrics resource maps by alias.
     */
    private MetricsResourceConfig mergeMetricsResource(MetricsResourceConfig base, MetricsResourceConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new MetricsResourceConfig(
                valueOrBase(override.type(), base.type()),
                valueOrBase(override.enabled(), base.enabled()),
                valueOrBase(override.url(), base.url()),
                valueOrBase(override.org(), base.org()),
                valueOrBase(override.bucket(), base.bucket()),
                valueOrBase(override.defaultAccess(), base.defaultAccess())
        );
    }

    /**
     * Merges Unix connection pool configuration.
     */
    private UnixPoolConfig mergeUnixPool(UnixPoolConfig base, UnixPoolConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new UnixPoolConfig(
                valueOrBase(override.size(), base.size()),
                valueOrBase(override.keepAliveSeconds(), base.keepAliveSeconds()),
                valueOrBase(override.borrowTimeoutSeconds(), base.borrowTimeoutSeconds())
        );
    }

    /**
     * Merges database connection pool configuration.
     */
    private DatabasePoolConfig mergeDatabasePool(DatabasePoolConfig base, DatabasePoolConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new DatabasePoolConfig(
                valueOrBase(override.enabled(), base.enabled()),
                valueOrBase(override.size(), base.size()),
                valueOrBase(override.borrowTimeoutSeconds(), base.borrowTimeoutSeconds()),
                valueOrBase(override.idleTimeoutSeconds(), base.idleTimeoutSeconds())
        );
    }

    /**
     * Merges maps by alias and applies a value merger for aliases present in
     * both base and override maps.
     */
    private <T> Map<String, T> mergeMap(
            Map<String, T> base,
            Map<String, T> override,
            ResourceMerger<T> merger
    ) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        Map<String, T> result = new HashMap<>(base);

        for (Map.Entry<String, T> entry : override.entrySet()) {
            String alias = entry.getKey();
            T baseValue = base.get(alias);
            T overrideValue = entry.getValue();

            result.put(alias, merger.merge(baseValue, overrideValue));
        }

        return result;
    }

    /**
     * Applies the standard override rule to scalar values.
     *
     * @param override overriding value.
     * @param base base value.
     * @param <T> value type.
     * @return override value when it is non-null, otherwise base value.
     */
    private <T> T valueOrBase(T override, T base) {
        return override != null ? override : base;
    }

    /**
     * Functional contract for merging one resource value by alias.
     *
     * @param <T> resource value type.
     */
    @FunctionalInterface
    private interface ResourceMerger<T> {

        /**
         * Merges a base resource value with an override resource value.
         *
         * @param base base value.
         * @param override override value.
         * @return merged value.
         */
        T merge(T base, T override);
    }
}
