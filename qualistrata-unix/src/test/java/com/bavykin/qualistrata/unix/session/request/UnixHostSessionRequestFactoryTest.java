package com.bavykin.qualistrata.unix.session.request;

import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionException;
import com.bavykin.qualistrata.unix.testsupport.UnixTestTargets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link UnixHostSessionRequestFactory}.
 *
 * <p>These tests use the generic resource/access/secrets model. They do not use
 * the removed legacy {@code TargetConfig.hosts()} structure.</p>
 */
class UnixHostSessionRequestFactoryTest {

    @Test
    void shouldFailWhenResolvedTargetIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixHostSessionRequestFactory((com.bavykin.qualistrata.core.config.model.ResolvedTarget) null)
        );

        assertEquals("Resolved target must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAliasIsNull() {
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(
                UnixTestTargets.validResolvedTarget()
        );

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> factory.create(null)
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAliasIsBlank() {
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(
                UnixTestTargets.validResolvedTarget()
        );

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> factory.create(" ")
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenTargetConfigurationDoesNotContainUnixResources() {
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(
                UnixTestTargets.emptyResolvedTarget()
        );

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> factory.create("app")
        );

        assertEquals("Target configuration does not contain Unix resources.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAliasIsNotConfigured() {
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(
                UnixTestTargets.validResolvedTarget()
        );

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> factory.create("missing")
        );

        assertEquals("Unix resource alias is not configured: missing", exception.getMessage());
    }

    @Test
    void shouldFailWhenSecretsDoNotContainUnixAccessProfiles() {
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(
                UnixTestTargets.targetWithoutSecrets()
        );

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> factory.create("app")
        );

        assertEquals("Secrets configuration does not contain Unix access profiles.", exception.getMessage());
    }

    @Test
    void shouldCreateRequestUsingDefaultAccessProfile() {
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(
                UnixTestTargets.validResolvedTarget()
        );

        UnixHostSessionRequest request = factory.create("app");

        assertEquals("app", request.hostAlias());
        assertEquals("app", request.resolvedUserAlias());
        assertNotNull(request.host());
        assertEquals("application", request.host().role());
        assertEquals("192.0.2.10", request.host().host());
        assertEquals(22, request.host().port());
        assertEquals("app", request.host().defaultUser());
        assertEquals("app-user", request.host().users().get("app").username());
        assertEquals("password", request.host().users().get("app").password());
    }

    @Test
    void shouldCreateRequestUsingExplicitAccessProfile() {
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(
                UnixTestTargets.validResolvedTarget()
        );

        UnixHostSessionRequest request = factory.create("app", "oracle");

        assertEquals("app", request.hostAlias());
        assertEquals("oracle", request.resolvedUserAlias());
        assertNotNull(request.host());
        assertEquals("oracle-user", request.host().users().get("oracle").username());
        assertEquals("password", request.host().users().get("oracle").password());
    }

    @Test
    void shouldTrimHostAliasAndAccessAlias() {
        UnixHostSessionRequestFactory factory = new UnixHostSessionRequestFactory(
                UnixTestTargets.validResolvedTarget()
        );

        UnixHostSessionRequest request = factory.create(" app ", " oracle ");

        assertEquals("app", request.hostAlias());
        assertEquals("oracle", request.resolvedUserAlias());
        assertNotNull(request.host());
        assertEquals("oracle-user", request.host().users().get("oracle").username());
    }
}
