package com.bavykin.qualistrata.core.config.merge;

import com.bavykin.qualistrata.core.config.model.APIConfig;
import com.bavykin.qualistrata.core.config.model.DBConfig;
import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.PerfConfig;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.config.model.UIConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link TargetConfigMerger}.
 *
 * <p>The tests document the configuration layering rules used by QualiStrata.
 * Public target configuration may provide open environment data, while secrets
 * or override layers may provide sensitive or environment-specific values. The
 * merger must combine such layers deterministically without knowing where the
 * layers came from.</p>
 */
class TargetConfigMergerTest {

    private final TargetConfigMerger merger = new TargetConfigMerger();

    @Test
    void shouldReturnOverrideWhenBaseIsNull() {
        TargetConfig override = target("override");

        TargetConfig merged = merger.merge(null, override);

        assertSame(override, merged);
    }

    @Test
    void shouldReturnBaseWhenOverrideIsNull() {
        TargetConfig base = target("base");

        TargetConfig merged = merger.merge(base, null);

        assertSame(base, merged);
    }

    @Test
    void shouldOverrideTopLevelEnvironmentNameWhenProvided() {
        TargetConfig base = target("base");
        TargetConfig override = new TargetConfig(
                "override",
                null,
                null,
                null,
                null,
                null
        );

        TargetConfig merged = merger.merge(base, override);

        assertEquals("override", merged.env());
    }

    @Test
    void shouldKeepTopLevelEnvironmentNameWhenOverrideIsNull() {
        TargetConfig base = target("base");
        TargetConfig override = new TargetConfig(
                null,
                null,
                null,
                null,
                null,
                null
        );

        TargetConfig merged = merger.merge(base, override);

        assertEquals("base", merged.env());
    }

    @Test
    void shouldMergeUserValuesFromPublicTargetAndSecretsLayer() {
        TargetConfig publicTarget = new TargetConfig(
                "demo",
                Map.of(
                        "app",
                        new HostConfig(
                                "application",
                                "app.demo.test",
                                22,
                                "app",
                                Map.of(
                                        "app",
                                        new UserConfig(
                                                "app",
                                                null,
                                                "/home/app/.ssh/id_rsa"
                                        )
                                ),
                                null,
                                null,
                                null,
                                null
                        )
                ),
                null,
                null,
                null,
                null
        );

        TargetConfig secretsLayer = new TargetConfig(
                null,
                Map.of(
                        "app",
                        new HostConfig(
                                null,
                                null,
                                null,
                                null,
                                Map.of(
                                        "app",
                                        new UserConfig(
                                                null,
                                                "resolved-secret",
                                                null
                                        )
                                ),
                                null,
                                null,
                                null,
                                null
                        )
                ),
                null,
                null,
                null,
                null
        );

        TargetConfig merged = merger.merge(publicTarget, secretsLayer);
        UserConfig user = merged.hosts().get("app").users().get("app");

        assertEquals("app", user.username());
        assertEquals("resolved-secret", user.password());
        assertEquals("/home/app/.ssh/id_rsa", user.keyPath());
    }

    @Test
    void shouldMergeHostValuesByAlias() {
        TargetConfig base = new TargetConfig(
                "demo",
                Map.of(
                        "app",
                        new HostConfig(
                                "application",
                                "app.demo.test",
                                22,
                                "app",
                                null,
                                "oracle_base",
                                "BASE",
                                new UnixPoolConfig(2, 30, 5),
                                60
                        )
                ),
                null,
                null,
                null,
                null
        );

        TargetConfig override = new TargetConfig(
                null,
                Map.of(
                        "app",
                        new HostConfig(
                                null,
                                "app.override.test",
                                null,
                                null,
                                null,
                                null,
                                "OVERRIDE",
                                new UnixPoolConfig(null, 120, null),
                                null
                        )
                ),
                null,
                null,
                null,
                null
        );

        TargetConfig merged = merger.merge(base, override);
        HostConfig app = merged.hosts().get("app");

        assertEquals("application", app.role());
        assertEquals("app.override.test", app.host());
        assertEquals(22, app.port());
        assertEquals("app", app.defaultUser());
        assertEquals("oracle_base", app.oracleUser());
        assertEquals("OVERRIDE", app.oracleService());
        assertEquals(2, app.pool().size());
        assertEquals(120, app.pool().keepAliveSeconds());
        assertEquals(5, app.pool().borrowTimeoutSeconds());
        assertEquals(60, app.commandTimeoutSeconds());
    }

    @Test
    void shouldAddNewHostFromOverrideLayer() {
        TargetConfig base = new TargetConfig(
                "demo",
                Map.of(
                        "app",
                        new HostConfig(
                                "application",
                                "app.demo.test",
                                22,
                                "app",
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                ),
                null,
                null,
                null,
                null
        );

        TargetConfig override = new TargetConfig(
                null,
                Map.of(
                        "db",
                        new HostConfig(
                                "database",
                                "db.demo.test",
                                22,
                                "oracle",
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                ),
                null,
                null,
                null,
                null
        );

        TargetConfig merged = merger.merge(base, override);

        assertEquals("app.demo.test", merged.hosts().get("app").host());
        assertEquals("db.demo.test", merged.hosts().get("db").host());
    }

    @Test
    void shouldMergeDatabaseConfigurationFieldByField() {
        TargetConfig base = new TargetConfig(
                "demo",
                null,
                null,
                new DBConfig(
                        "db.demo.test",
                        1521,
                        null,
                        "DEMO",
                        "base_user"
                ),
                null,
                null
        );

        TargetConfig override = new TargetConfig(
                null,
                null,
                null,
                new DBConfig(
                        null,
                        null,
                        "DEMO_SERVICE",
                        null,
                        "override_user"
                ),
                null,
                null
        );

        TargetConfig merged = merger.merge(base, override);

        assertEquals("db.demo.test", merged.db().host());
        assertEquals(1521, merged.db().port());
        assertEquals("DEMO_SERVICE", merged.db().service());
        assertEquals("DEMO", merged.db().sid());
        assertEquals("override_user", merged.db().username());
    }

    @Test
    void shouldMergeUiConfigurationFieldByField() {
        TargetConfig base = new TargetConfig(
                "demo",
                null,
                null,
                null,
                new UIConfig("https://app.demo.test"),
                null
        );

        TargetConfig override = new TargetConfig(
                null,
                null,
                null,
                null,
                new UIConfig("https://app.override.test"),
                null
        );

        TargetConfig merged = merger.merge(base, override);

        assertEquals("https://app.override.test", merged.ui().baseUrl());
    }

    @Test
    void shouldKeepApiAndPerfConfigurationSectionsWhenOverrideIsNull() {
        APIConfig api = new APIConfig();
        PerfConfig perf = new PerfConfig();

        TargetConfig base = new TargetConfig(
                "demo",
                null,
                api,
                null,
                null,
                perf
        );

        TargetConfig override = new TargetConfig(
                null,
                null,
                null,
                null,
                null,
                null
        );

        TargetConfig merged = merger.merge(base, override);

        assertSame(api, merged.api());
        assertSame(perf, merged.perf());
    }

    @Test
    void shouldReplaceApiAndPerfConfigurationSectionsWhenOverrideIsProvided() {
        APIConfig baseApi = new APIConfig();
        APIConfig overrideApi = new APIConfig();
        PerfConfig basePerf = new PerfConfig();
        PerfConfig overridePerf = new PerfConfig();

        TargetConfig base = new TargetConfig(
                "demo",
                null,
                baseApi,
                null,
                null,
                basePerf
        );

        TargetConfig override = new TargetConfig(
                null,
                null,
                overrideApi,
                null,
                null,
                overridePerf
        );

        TargetConfig merged = merger.merge(base, override);

        assertSame(overrideApi, merged.api());
        assertSame(overridePerf, merged.perf());
    }

    /**
     * Creates a minimal target configuration for tests where only identity is
     * important.
     *
     * @param env logical environment name.
     * @return minimal target configuration.
     */
    private TargetConfig target(String env) {
        return new TargetConfig(
                env,
                null,
                null,
                null,
                null,
                null
        );
    }
}