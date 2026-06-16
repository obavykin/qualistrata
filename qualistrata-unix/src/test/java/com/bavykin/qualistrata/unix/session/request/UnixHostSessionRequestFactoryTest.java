package com.bavykin.qualistrata.unix.session.request;

import com.bavykin.qualistrata.core.config.model.APIConfig;
import com.bavykin.qualistrata.core.config.model.DBConfig;
import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.PerfConfig;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.config.model.UIConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link UnixHostSessionRequestFactory}.
 */
class UnixHostSessionRequestFactoryTest {

    @Test
    void shouldFailWhenTargetConfigurationIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixHostSessionRequestFactory(null)
        );

        assertEquals("Target configuration must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAliasIsNull() {
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(validTargetConfig());

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> factory.create(null)
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAliasIsBlank() {
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(validTargetConfig());

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> factory.create(" ")
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenTargetConfigurationDoesNotContainHosts() {
        TargetConfig config = new TargetConfig(
                "dev",
                null,
                new APIConfig(),
                new DBConfig(null, null, null, null, null),
                new UIConfig("https://example.test"),
                new PerfConfig()
        );

        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(config);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> factory.create("app")
        );

        assertEquals("Target configuration does not contain Unix hosts.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAliasIsNotConfigured() {
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(validTargetConfig());

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> factory.create("missing")
        );

        assertEquals("Unix host alias is not configured: missing", exception.getMessage());
    }

    @Test
    void shouldCreateRequestUsingDefaultUser() {
        TargetConfig config = validTargetConfig();
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(config);

        UnixHostSessionRequest request = factory.create("app");

        assertEquals("app", request.hostAlias());
        assertSame(config.hosts().get("app"), request.host());
        assertEquals("app", request.resolvedUserAlias());
    }

    @Test
    void shouldCreateRequestUsingExplicitUserAlias() {
        TargetConfig config = validTargetConfig();
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(config);

        UnixHostSessionRequest request = factory.create("app", "oracle");

        assertEquals("app", request.hostAlias());
        assertSame(config.hosts().get("app"), request.host());
        assertEquals("oracle", request.resolvedUserAlias());
    }

    @Test
    void shouldTrimHostAliasAndUserAlias() {
        TargetConfig config = validTargetConfig();
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(config);

        UnixHostSessionRequest request = factory.create(" app ", " oracle ");

        assertEquals("app", request.hostAlias());
        assertSame(config.hosts().get("app"), request.host());
        assertEquals("oracle", request.resolvedUserAlias());
    }

    /**
     * Creates a valid target configuration for tests.
     *
     * @return target configuration.
     */
    private TargetConfig validTargetConfig() {
        return new TargetConfig(
                "dev",
                Map.of(
                        "app",
                        validHost()
                ),
                new APIConfig(),
                new DBConfig(null, null, null, null, null),
                new UIConfig("https://example.test"),
                new PerfConfig()
        );
    }

    /**
     * Creates a valid host configuration for tests.
     *
     * @return host configuration.
     */
    private HostConfig validHost() {
        return new HostConfig(
                "application",
                "192.0.2.10",
                22,
                "app",
                Map.of(
                        "app",
                        new UserConfig(
                                "app-user",
                                "password",
                                null
                        ),
                        "oracle",
                        new UserConfig(
                                "oracle-user",
                                "password",
                                null
                        )
                ),
                null,
                null,
                new UnixPoolConfig(
                        1,
                        60,
                        5
                ),
                10
        );
    }
}