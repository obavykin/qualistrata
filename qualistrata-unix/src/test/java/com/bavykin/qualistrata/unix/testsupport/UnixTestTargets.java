package com.bavykin.qualistrata.unix.testsupport;

import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.model.ResourcesConfig;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UnixResourceConfig;
import com.bavykin.qualistrata.core.config.secrets.AccessSecretsConfig;
import com.bavykin.qualistrata.core.config.secrets.SecretsConfig;
import com.bavykin.qualistrata.core.config.secrets.UnixAccessSecret;

import java.util.Map;

/**
 * Test fixtures for the generic QualiStrata Unix resource/access model.
 *
 * <p>The fixtures intentionally use generic aliases such as {@code app} and
 * {@code oracle}. They do not represent TNT-specific conventions. They only
 * provide small resolved targets for Unix module unit tests.</p>
 */
public final class UnixTestTargets {

    private UnixTestTargets() {
        // Utility class.
    }

    /**
     * Creates a resolved target with no Unix resources or access profiles.
     *
     * @return empty resolved target.
     */
    public static ResolvedTarget emptyResolvedTarget() {
        return new ResolvedTarget(
                "test",
                new TargetConfig(
                        "test",
                        new ResourcesConfig(
                                Map.of(),
                                Map.of(),
                                Map.of(),
                                Map.of(),
                                Map.of()
                        )
                ),
                new SecretsConfig(
                        new AccessSecretsConfig(
                                Map.of(),
                                Map.of(),
                                Map.of(),
                                Map.of(),
                                Map.of()
                        )
                )
        );
    }

    /**
     * Creates a resolved target with one Unix host and two access profiles.
     *
     * @return valid resolved target.
     */
    public static ResolvedTarget validResolvedTarget() {
        return new ResolvedTarget(
                "dev",
                validTargetConfig(),
                validSecretsConfig()
        );
    }

    /**
     * Creates a target that contains resources but no secrets.
     *
     * @return resolved target with missing secrets.
     */
    public static ResolvedTarget targetWithoutSecrets() {
        return new ResolvedTarget(
                "dev",
                validTargetConfig(),
                new SecretsConfig(
                        new AccessSecretsConfig(
                                Map.of(),
                                Map.of(),
                                Map.of(),
                                Map.of(),
                                Map.of()
                        )
                )
        );
    }

    private static TargetConfig validTargetConfig() {
        return new TargetConfig(
                "dev",
                new ResourcesConfig(
                        Map.of(),
                        Map.of(
                                "app",
                                new UnixResourceConfig(
                                        "application",
                                        "192.0.2.10",
                                        22,
                                        "app",
                                        10,
                                        new UnixPoolConfig(
                                                1,
                                                60,
                                                5
                                        )
                                )
                        ),
                        Map.of(),
                        Map.of(),
                        Map.of()
                )
        );
    }

    private static SecretsConfig validSecretsConfig() {
        return new SecretsConfig(
                new AccessSecretsConfig(
                        Map.of(),
                        Map.of(
                                "app",
                                Map.of(
                                        "app",
                                        new UnixAccessSecret(
                                                "app-user",
                                                "password",
                                                null,
                                                null
                                        ),
                                        "oracle",
                                        new UnixAccessSecret(
                                                "oracle-user",
                                                "password",
                                                null,
                                                null
                                        )
                                )
                        ),
                        Map.of(),
                        Map.of(),
                        Map.of()
                )
        );
    }
}
