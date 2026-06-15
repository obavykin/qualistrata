package com.bavykin.qualistrata.unix.session;

import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link UnixHostSessionRequestValidator}.
 *
 * <p>The tests verify generic session request validation rules before any real
 * SSH or pooling implementation is introduced.</p>
 */
class UnixHostSessionRequestValidatorTest {

    private final UnixHostSessionRequestValidator validator = new UnixHostSessionRequestValidator();

    @Test
    void shouldAcceptValidRequestWithExplicitUser() {
        UnixHostSessionRequest request = new UnixHostSessionRequest(
                "app",
                validHost(),
                "app"
        );

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void shouldAcceptValidRequestWithDefaultUser() {
        UnixHostSessionRequest request = new UnixHostSessionRequest(
                "app",
                validHost(),
                null
        );

        assertDoesNotThrow(() -> validator.validate(request));
        assertEquals("app", request.resolvedUserAlias());
        assertEquals("app", request.resolvedUser().username());
    }

    @Test
    void shouldFailWhenRequestIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(null)
        );

        assertEquals("Unix host session request must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAliasIsBlank() {
        UnixHostSessionRequest request = new UnixHostSessionRequest(
                " ",
                validHost(),
                "app"
        );

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(request)
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostConfigurationIsNull() {
        UnixHostSessionRequest request = new UnixHostSessionRequest(
                "app",
                null,
                "app"
        );

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(request)
        );

        assertEquals("Unix host configuration must not be null for alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostAddressIsBlank() {
        HostConfig host = new HostConfig(
                "application",
                " ",
                22,
                "app",
                Map.of("app", new UserConfig("app", null, null)),
                null,
                null,
                null,
                null
        );

        UnixHostSessionRequest request = new UnixHostSessionRequest("app", host, "app");

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(request)
        );

        assertEquals("Unix host address must not be blank for alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenHostPortIsNotPositive() {
        HostConfig host = new HostConfig(
                "application",
                "app.demo.test",
                0,
                "app",
                Map.of("app", new UserConfig("app", null, null)),
                null,
                null,
                null,
                null
        );

        UnixHostSessionRequest request = new UnixHostSessionRequest("app", host, "app");

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(request)
        );

        assertEquals("Unix host port must be positive for alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenUserAliasCannotBeResolved() {
        HostConfig host = new HostConfig(
                "application",
                "app.demo.test",
                22,
                null,
                Map.of("app", new UserConfig("app", null, null)),
                null,
                null,
                null,
                null
        );

        UnixHostSessionRequest request = new UnixHostSessionRequest("app", host, null);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(request)
        );

        assertEquals(
                "Unix user alias must be provided or default user must be configured for host alias: app",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenUsersAreNotConfigured() {
        HostConfig host = new HostConfig(
                "application",
                "app.demo.test",
                22,
                "app",
                null,
                null,
                null,
                null,
                null
        );

        UnixHostSessionRequest request = new UnixHostSessionRequest("app", host, null);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(request)
        );

        assertEquals("Unix users must be configured for host alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenUserDoesNotExist() {
        UnixHostSessionRequest request = new UnixHostSessionRequest(
                "app",
                validHost(),
                "missing"
        );

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(request)
        );

        assertEquals("Unix user 'missing' is not defined for host alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenUsernameIsBlank() {
        HostConfig host = new HostConfig(
                "application",
                "app.demo.test",
                22,
                "app",
                Map.of("app", new UserConfig(" ", null, null)),
                null,
                null,
                null,
                null
        );

        UnixHostSessionRequest request = new UnixHostSessionRequest("app", host, null);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(request)
        );

        assertEquals(
                "Unix username must not be blank for user alias: app on host alias: app",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenPoolSizeIsNotPositive() {
        HostConfig host = new HostConfig(
                "application",
                "app.demo.test",
                22,
                "app",
                Map.of("app", new UserConfig("app", null, null)),
                null,
                null,
                new UnixPoolConfig(0, 60, 10),
                null
        );

        UnixHostSessionRequest request = new UnixHostSessionRequest("app", host, null);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(request)
        );

        assertEquals("Unix session pool size must be positive for host alias: app", exception.getMessage());
    }

    @Test
    void shouldFailWhenPoolKeepAliveIsNegative() {
        HostConfig host = new HostConfig(
                "application",
                "app.demo.test",
                22,
                "app",
                Map.of("app", new UserConfig("app", null, null)),
                null,
                null,
                new UnixPoolConfig(5, -1, 10),
                null
        );

        UnixHostSessionRequest request = new UnixHostSessionRequest("app", host, null);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(request)
        );

        assertEquals(
                "Unix session pool keep-alive seconds must be non-negative for host alias: app",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenPoolBorrowTimeoutIsNotPositive() {
        HostConfig host = new HostConfig(
                "application",
                "app.demo.test",
                22,
                "app",
                Map.of("app", new UserConfig("app", null, null)),
                null,
                null,
                new UnixPoolConfig(5, 60, 0),
                null
        );

        UnixHostSessionRequest request = new UnixHostSessionRequest("app", host, null);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> validator.validate(request)
        );

        assertEquals(
                "Unix session pool borrow timeout seconds must be positive for host alias: app",
                exception.getMessage()
        );
    }

    /**
     * Creates a valid host configuration for session request tests.
     *
     * @return valid host configuration.
     */
    private HostConfig validHost() {
        return new HostConfig(
                "application",
                "app.demo.test",
                22,
                "app",
                Map.of("app", new UserConfig("app", null, "/home/app/.ssh/id_rsa")),
                null,
                null,
                new UnixPoolConfig(5, 60, 10),
                30
        );
    }
}