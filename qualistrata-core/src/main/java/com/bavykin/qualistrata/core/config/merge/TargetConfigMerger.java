package com.bavykin.qualistrata.core.config.merge;

import com.bavykin.qualistrata.core.config.model.APIConfig;
import com.bavykin.qualistrata.core.config.model.DBConfig;
import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.PerfConfig;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.config.model.UIConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Merges layered target configuration objects into one effective configuration.
 *
 * <p>QualiStrata separates committed target configuration from runtime secret
 * and override layers. For example, a public target file may define hostnames,
 * usernames, ports, and key paths, while a local uncommitted secrets file may
 * provide passwords. This merger combines such layers without knowing where
 * they came from.</p>
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
                mergeHosts(base.hosts(), override.hosts()),
                mergeApi(base.api(), override.api()),
                mergeDb(base.db(), override.db()),
                mergeUi(base.ui(), override.ui()),
                mergePerf(base.perf(), override.perf())
        );
    }

    /**
     * Merges host maps by host alias.
     *
     * <p>Existing host aliases are merged recursively. New aliases from the
     * override layer are added to the result.</p>
     *
     * @param base base host map.
     * @param override overriding host map.
     * @return merged host map.
     */
    private Map<String, HostConfig> mergeHosts(
            Map<String, HostConfig> base,
            Map<String, HostConfig> override
    ) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        Map<String, HostConfig> result = new HashMap<>(base);

        for (Map.Entry<String, HostConfig> entry : override.entrySet()) {
            String hostAlias = entry.getKey();
            HostConfig baseHost = base.get(hostAlias);
            HostConfig overrideHost = entry.getValue();

            result.put(hostAlias, mergeHost(baseHost, overrideHost));
        }

        return result;
    }

    /**
     * Merges one logical host configuration.
     *
     * <p>Scalar host properties use the standard non-null override rule.
     * Nested users and connection pool settings are merged recursively.</p>
     *
     * @param base base host configuration.
     * @param override overriding host configuration.
     * @return merged host configuration.
     */
    private HostConfig mergeHost(HostConfig base, HostConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new HostConfig(
                valueOrBase(override.role(), base.role()),
                valueOrBase(override.host(), base.host()),
                valueOrBase(override.port(), base.port()),
                valueOrBase(override.defaultUser(), base.defaultUser()),
                mergeUsers(base.users(), override.users()),
                valueOrBase(override.oracleUser(), base.oracleUser()),
                valueOrBase(override.oracleService(), base.oracleService()),
                mergePool(base.pool(), override.pool()),
                valueOrBase(override.commandTimeoutSeconds(), base.commandTimeoutSeconds())
        );
    }

    /**
     * Merges user maps by logical user alias.
     *
     * <p>Existing user aliases are merged recursively. New aliases from the
     * override layer are added to the result.</p>
     *
     * @param base base user map.
     * @param override overriding user map.
     * @return merged user map.
     */
    private Map<String, UserConfig> mergeUsers(
            Map<String, UserConfig> base,
            Map<String, UserConfig> override
    ) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        Map<String, UserConfig> result = new HashMap<>(base);

        for (Map.Entry<String, UserConfig> entry : override.entrySet()) {
            String userAlias = entry.getKey();
            UserConfig baseUser = base.get(userAlias);
            UserConfig overrideUser = entry.getValue();

            result.put(userAlias, mergeUser(baseUser, overrideUser));
        }

        return result;
    }

    /**
     * Merges one logical user configuration.
     *
     * <p>This method supports the common configuration layering model where
     * public target YAML provides non-secret values such as username or key path,
     * while a local secrets layer provides the password. The merged user can
     * therefore contain values originating from different configuration layers.</p>
     *
     * @param base base user configuration.
     * @param override overriding user configuration.
     * @return merged user configuration.
     */
    private UserConfig mergeUser(UserConfig base, UserConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new UserConfig(
                valueOrBase(override.username(), base.username()),
                valueOrBase(override.password(), base.password()),
                valueOrBase(override.keyPath(), base.keyPath())
        );
    }

    /**
     * Merges Unix connection pool configuration.
     *
     * <p>Each field is merged independently so that a secrets or override layer
     * may change only one pool setting without repeating the whole pool
     * configuration.</p>
     *
     * @param base base pool configuration.
     * @param override overriding pool configuration.
     * @return merged pool configuration.
     */
    private UnixPoolConfig mergePool(UnixPoolConfig base, UnixPoolConfig override) {
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
     * Merges UI configuration.
     *
     * <p>The method exists even though the current UI model contains only one
     * field. Keeping UI merge logic isolated makes future extension safe when
     * additional generic UI settings are introduced.</p>
     *
     * @param base base UI configuration.
     * @param override overriding UI configuration.
     * @return merged UI configuration.
     */
    private UIConfig mergeUi(UIConfig base, UIConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new UIConfig(
                valueOrBase(override.baseUrl(), base.baseUrl())
        );
    }

    /**
     * Merges database configuration.
     *
     * <p>Each database field is merged independently so that secret or override
     * layers may provide only the values they need to change.</p>
     *
     * @param base base database configuration.
     * @param override overriding database configuration.
     * @return merged database configuration.
     */
    private DBConfig mergeDb(DBConfig base, DBConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return new DBConfig(
                valueOrBase(override.host(), base.host()),
                valueOrBase(override.port(), base.port()),
                valueOrBase(override.service(), base.service()),
                valueOrBase(override.sid(), base.sid()),
                valueOrBase(override.username(), base.username())
        );
    }

    /**
     * Merges API configuration.
     *
     * <p>The current API model is intentionally empty. The method still exists
     * to make the all-sections-deep-merge rule explicit and to provide the
     * extension point for future generic API settings.</p>
     *
     * @param base base API configuration.
     * @param override overriding API configuration.
     * @return merged API configuration.
     */
    private APIConfig mergeApi(APIConfig base, APIConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return override;
    }

    /**
     * Merges performance configuration.
     *
     * <p>The current performance model is intentionally empty. The method still
     * exists to make the all-sections-deep-merge rule explicit and to provide
     * the extension point for future metrics and telemetry settings.</p>
     *
     * @param base base performance configuration.
     * @param override overriding performance configuration.
     * @return merged performance configuration.
     */
    private PerfConfig mergePerf(PerfConfig base, PerfConfig override) {
        if (base == null) {
            return override;
        }

        if (override == null) {
            return base;
        }

        return override;
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
}