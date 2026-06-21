package com.bavykin.qualistrata.core.config.merge;

import com.bavykin.qualistrata.core.config.model.DatabasePoolConfig;
import com.bavykin.qualistrata.core.config.model.DatabaseResourceConfig;
import com.bavykin.qualistrata.core.config.model.ResourcesConfig;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.config.model.UiResourceConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UnixResourceConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link TargetConfigMerger}.
 *
 * <p>The tests document the configuration layering rules used by QualiStrata.
 * Public target configuration may provide open environment data, while override
 * layers may provide environment-specific non-secret changes. The merger must
 * combine such layers deterministically without knowing where the layers came
 * from.</p>
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
        TargetConfig override = new TargetConfig("override", null, null);

        TargetConfig merged = merger.merge(base, override);

        assertEquals("override", merged.env());
    }

    @Test
    void shouldKeepTopLevelEnvironmentNameWhenOverrideIsNull() {
        TargetConfig base = target("base");
        TargetConfig override = new TargetConfig(null, null, null);

        TargetConfig merged = merger.merge(base, override);

        assertEquals("base", merged.env());
    }

    @Test
    void shouldOverrideReleaseIdentifierWhenProvided() {
        TargetConfig base = new TargetConfig("pt-a", "release_6.6", null);
        TargetConfig override = new TargetConfig(null, "release_6.7", null);

        TargetConfig merged = merger.merge(base, override);

        assertEquals("release_6.7", merged.release());
    }

    @Test
    void shouldKeepReleaseIdentifierWhenOverrideIsNull() {
        TargetConfig base = new TargetConfig("pt-a", "release_6.6", null);
        TargetConfig override = new TargetConfig(null, null, null);

        TargetConfig merged = merger.merge(base, override);

        assertEquals("release_6.6", merged.release());
    }

    @Test
    void shouldMergeUiResourcesByAlias() {
        TargetConfig base = new TargetConfig(
                "demo",
                new ResourcesConfig(
                        Map.of("tracker", new UiResourceConfig("web-ui", "https://base", "admin")),
                        null,
                        null,
                        null,
                        null
                )
        );
        TargetConfig override = new TargetConfig(
                null,
                new ResourcesConfig(
                        Map.of("tracker", new UiResourceConfig(null, "https://override", null)),
                        null,
                        null,
                        null,
                        null
                )
        );

        TargetConfig merged = merger.merge(base, override);
        UiResourceConfig tracker = merged.resources().ui().get("tracker");

        assertEquals("web-ui", tracker.type());
        assertEquals("https://override", tracker.baseUrl());
        assertEquals("admin", tracker.defaultAccess());
    }

    @Test
    void shouldMergeUnixResourcesAndPoolByAlias() {
        TargetConfig base = new TargetConfig(
                "demo",
                new ResourcesConfig(
                        null,
                        Map.of("app", new UnixResourceConfig(
                                "application",
                                "app.base.test",
                                22,
                                "tracker",
                                60,
                                new UnixPoolConfig(2, 30, 5)
                        )),
                        null,
                        null,
                        null
                )
        );
        TargetConfig override = new TargetConfig(
                null,
                new ResourcesConfig(
                        null,
                        Map.of("app", new UnixResourceConfig(
                                null,
                                "app.override.test",
                                null,
                                null,
                                null,
                                new UnixPoolConfig(null, 120, null)
                        )),
                        null,
                        null,
                        null
                )
        );

        TargetConfig merged = merger.merge(base, override);
        UnixResourceConfig app = merged.resources().unix().get("app");

        assertEquals("application", app.role());
        assertEquals("app.override.test", app.host());
        assertEquals(22, app.port());
        assertEquals("tracker", app.defaultAccess());
        assertEquals(60, app.commandTimeoutSeconds());
        assertEquals(2, app.pool().size());
        assertEquals(120, app.pool().keepAliveSeconds());
        assertEquals(5, app.pool().borrowTimeoutSeconds());
    }

    @Test
    void shouldMergeDatabaseResourcesAndPoolByAlias() {
        TargetConfig base = new TargetConfig(
                "demo",
                new ResourcesConfig(
                        null,
                        null,
                        Map.of("main", new DatabaseResourceConfig(
                                "oracle",
                                "db.base.test",
                                1521,
                                "BASE",
                                "SAGSYS",
                                null,
                                "sid",
                                "sagsys",
                                10,
                                30,
                                new DatabasePoolConfig(false, 2, 5, 60)
                        )),
                        null,
                        null
                )
        );
        TargetConfig override = new TargetConfig(
                null,
                new ResourcesConfig(
                        null,
                        null,
                        Map.of("main", new DatabaseResourceConfig(
                                null,
                                "db.override.test",
                                null,
                                "OVERRIDE",
                                null,
                                null,
                                null,
                                null,
                                null,
                                45,
                                new DatabasePoolConfig(true, null, 15, null)
                        )),
                        null,
                        null
                )
        );

        TargetConfig merged = merger.merge(base, override);
        DatabaseResourceConfig main = merged.resources().databases().get("main");

        assertEquals("oracle", main.type());
        assertEquals("db.override.test", main.host());
        assertEquals(1521, main.port());
        assertEquals("OVERRIDE", main.databaseName());
        assertEquals("SAGSYS", main.schema());
        assertEquals("sid", main.connectionMode());
        assertEquals("sagsys", main.defaultAccess());
        assertEquals(10, main.connectionTimeoutSeconds());
        assertEquals(45, main.queryTimeoutSeconds());
        assertEquals(true, main.pool().enabled());
        assertEquals(2, main.pool().size());
        assertEquals(15, main.pool().borrowTimeoutSeconds());
        assertEquals(60, main.pool().idleTimeoutSeconds());
    }

    @Test
    void shouldAddNewResourcesFromOverrideLayer() {
        TargetConfig base = new TargetConfig(
                "demo",
                new ResourcesConfig(
                        null,
                        Map.of("app", new UnixResourceConfig("application", "app.base.test", 22, null, null, null)),
                        null,
                        null,
                        null
                )
        );
        TargetConfig override = new TargetConfig(
                null,
                new ResourcesConfig(
                        null,
                        Map.of("db-host", new UnixResourceConfig("database", "db.base.test", 22, null, null, null)),
                        null,
                        null,
                        null
                )
        );

        TargetConfig merged = merger.merge(base, override);

        assertEquals("app.base.test", merged.resources().unix().get("app").host());
        assertEquals("db.base.test", merged.resources().unix().get("db-host").host());
    }

    private TargetConfig target(String env) {
        return new TargetConfig(env, new ResourcesConfig(null, null, null, null, null));
    }
}
