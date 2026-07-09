package com.bavykin.qualistrata.unix.ssh;

import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.unix.session.UnixHostSession;
import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;
import com.bavykin.qualistrata.unix.session.UnixSessionException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SshUnixHostSessionFactory}.
 *
 * <p>The tests verify conversion from generic Unix host session requests to
 * low-level SSH connection requests without using a concrete SSH library.</p>
 */
class SshUnixHostSessionFactoryTest {

    @Test
    void shouldOpenSshBackedUnixHostSession() {
        RecordingSshConnectionFactory connectionFactory = new RecordingSshConnectionFactory();

        SshUnixHostSessionFactory sessionFactory = new SshUnixHostSessionFactory(connectionFactory);

        UnixHostSession session = sessionFactory.open(validRequest());

        assertInstanceOf(SshUnixHostSession.class, session);
        assertTrue(session.isOpen());

        SshConnectionRequest connectionRequest = connectionFactory.lastRequest();

        assertEquals("app.demo.test", connectionRequest.host());
        assertEquals(22, connectionRequest.port());
        assertEquals("app", connectionRequest.username());
        assertEquals("secret", connectionRequest.password());
        assertEquals("/home/app/.ssh/id_rsa", connectionRequest.keyPath());
        assertEquals(Duration.ofSeconds(30), connectionRequest.connectTimeout());
        assertEquals(1, connectionFactory.openCount());
    }

    @Test
    void shouldUseDefaultSshPortWhenHostPortIsMissing() {
        RecordingSshConnectionFactory connectionFactory = new RecordingSshConnectionFactory();

        SshUnixHostSessionFactory sessionFactory = new SshUnixHostSessionFactory(connectionFactory);

        sessionFactory.open(requestWithHostPort(null));

        assertEquals(22, connectionFactory.lastRequest().port());
    }

    @Test
    void shouldFailWhenConnectionFactoryIsNull() {
        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> new SshUnixHostSessionFactory(null)
        );

        assertEquals("SSH connection factory must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenConnectionFactoryReturnsNull() {
        SshUnixHostSessionFactory sessionFactory = new SshUnixHostSessionFactory(
                request -> null
        );

        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> sessionFactory.open(validRequest())
        );

        assertEquals("SSH connection factory returned null connection.", exception.getMessage());
    }

    @Test
    void shouldValidateRequestBeforeOpeningConnection() {
        RecordingSshConnectionFactory connectionFactory = new RecordingSshConnectionFactory();

        SshUnixHostSessionFactory sessionFactory = new SshUnixHostSessionFactory(connectionFactory);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> sessionFactory.open(new UnixHostSessionRequest(" ", validHost(), "app"))
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
        assertEquals(0, connectionFactory.openCount());
    }

    /**
     * Creates a valid Unix host session request.
     *
     * @return valid session request.
     */
    private UnixHostSessionRequest validRequest() {
        return new UnixHostSessionRequest(
                "app",
                validHost(),
                "app"
        );
    }

    /**
     * Creates a valid request with custom host port.
     *
     * @param port host port.
     * @return request with custom host port.
     */
    private UnixHostSessionRequest requestWithHostPort(Integer port) {
        return new UnixHostSessionRequest(
                "app",
                new HostConfig(
                        "application",
                        "app.demo.test",
                        port,
                        "app",
                        Map.of("app", new UserConfig("app", "secret", "/home/app/.ssh/id_rsa")),
                        null,
                        null,
                        new UnixPoolConfig(5, 60, 10),
                        30
                ),
                "app"
        );
    }

    /**
     * Creates a valid host configuration for factory tests.
     *
     * @return valid host configuration.
     */
    private HostConfig validHost() {
        return new HostConfig(
                "application",
                "app.demo.test",
                22,
                "app",
                Map.of("app", new UserConfig("app", "secret", "/home/app/.ssh/id_rsa")),
                null,
                null,
                new UnixPoolConfig(5, 60, 10),
                30
        );
    }

    /**
     * SSH connection factory that records requests and returns fake connections.
     */
    private static class RecordingSshConnectionFactory implements SshConnectionFactory {

        private int openCount;
        private SshConnectionRequest lastRequest;

        /**
         * Records connection request and returns a fake SSH connection.
         *
         * @param request SSH connection request.
         * @return fake SSH connection.
         */
        @Override
        public SshConnection open(SshConnectionRequest request) {
            openCount++;
            lastRequest = request;

            return new FakeSshConnection();
        }

        /**
         * @return number of opened connections.
         */
        private int openCount() {
            return openCount;
        }

        /**
         * @return last SSH connection request.
         */
        private SshConnectionRequest lastRequest() {
            return lastRequest;
        }
    }

    /**
     * Fake SSH connection used by factory tests.
     */
    private static class FakeSshConnection implements SshConnection {

        private boolean open = true;

        /**
         * Executes a command and returns a successful result.
         *
         * @param request command execution request.
         * @return successful command result.
         */
        @Override
        public com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult execute(
                com.bavykin.qualistrata.unix.command.UnixCommandRequest request
        ) {
            return new com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult(0, "", "");
        }

        /**
         * @return whether the fake connection is open.
         */
        @Override
        public boolean isOpen() {
            return open;
        }

        /**
         * Closes this fake connection.
         */
        @Override
        public void close() {
            open = false;
        }
    }
}