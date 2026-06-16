package com.bavykin.qualistrata.unix.session.factory;

import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.session.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UnixSessionProviderFactory}.
 */
class UnixSessionProviderFactoryTest {

    @Test
    void shouldFailWhenHostSessionFactoryIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new UnixSessionProviderFactory((UnixHostSessionFactory) null)
        );

        assertEquals("Unix host session factory must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenProviderTypeIsNull() {
        UnixSessionProviderFactory factory = new UnixSessionProviderFactory(new RecordingUnixHostSessionFactory());

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> factory.create(null)
        );

        assertEquals("Unix session provider type must not be null.", exception.getMessage());
    }

    @Test
    void shouldCreateDirectProvider() {
        RecordingUnixHostSessionFactory hostSessionFactory = new RecordingUnixHostSessionFactory();
        UnixSessionProviderFactory factory = new UnixSessionProviderFactory(hostSessionFactory);

        UnixSessionProvider provider = factory.create(UnixSessionProviderType.DIRECT);

        UnixHostSession firstSession = provider.open(validRequest());
        UnixHostSession secondSession = provider.open(validRequest());

        assertNotSame(firstSession, secondSession);
        assertEquals(2, hostSessionFactory.openCount());

        firstSession.close();
        secondSession.close();
        provider.close();
    }

    @Test
    void shouldCreateDirectProviderUsingConvenienceMethod() {
        RecordingUnixHostSessionFactory hostSessionFactory = new RecordingUnixHostSessionFactory();
        UnixSessionProviderFactory factory = new UnixSessionProviderFactory(hostSessionFactory);

        UnixSessionProvider provider = factory.createDirect();

        UnixHostSession session = provider.open(validRequest());

        assertEquals(1, hostSessionFactory.openCount());

        session.close();
        provider.close();
    }

    @Test
    void shouldCreatePooledProvider() {
        RecordingUnixHostSessionFactory hostSessionFactory = new RecordingUnixHostSessionFactory();
        UnixSessionProviderFactory factory = new UnixSessionProviderFactory(hostSessionFactory);

        UnixSessionProvider provider = factory.create(UnixSessionProviderType.POOLED);

        UnixHostSession firstSession = provider.open(validRequest());
        String firstPhysicalSessionId = firstSessionDelegate(firstSession);
        firstSession.close();

        UnixHostSession secondSession = provider.open(validRequest());
        String secondPhysicalSessionId = firstSessionDelegate(secondSession);

        assertEquals(firstPhysicalSessionId, secondPhysicalSessionId);
        assertEquals(1, hostSessionFactory.openCount());

        secondSession.close();
        provider.close();
    }

    @Test
    void shouldCreatePooledProviderUsingConvenienceMethod() {
        RecordingUnixHostSessionFactory hostSessionFactory = new RecordingUnixHostSessionFactory();
        UnixSessionProviderFactory factory = new UnixSessionProviderFactory(hostSessionFactory);

        UnixSessionProvider provider = factory.createPooled();

        UnixHostSession session = provider.open(validRequest());

        assertEquals(1, hostSessionFactory.openCount());

        session.close();
        provider.close();
    }

    /**
     * Creates a valid Unix host session request.
     *
     * @return valid request.
     */
    private UnixHostSessionRequest validRequest() {
        return new UnixHostSessionRequest(
                "app",
                validHost(),
                "app"
        );
    }

    /**
     * Creates a valid host configuration for tests.
     *
     * @return valid host configuration.
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

    /**
     * Extracts the physical fake session from a pooled wrapper.
     *
     * <p>The production pooled provider returns wrapper sessions. The fake
     * command result exposes the physical fake session identifier so the test
     * can verify whether pooling reused the same physical session.</p>
     *
     * @param session pooled or direct session.
     * @return physical fake session identifier.
     */
    private String firstSessionDelegate(UnixHostSession session) {
        CommandResult result = session.execute(new UnixCommandRequest("id", null));

        return result.stdout();
    }

    /**
     * Fake Unix host session factory that creates numbered fake sessions.
     */
    private static class RecordingUnixHostSessionFactory implements UnixHostSessionFactory {

        private int openCount;

        /**
         * Opens a new numbered fake session.
         *
         * @param request Unix host session request.
         * @return fake Unix host session.
         */
        @Override
        public UnixHostSession open(UnixHostSessionRequest request) {
            openCount++;

            return new FakeUnixHostSession("session-" + openCount);
        }

        /**
         * Returns how many physical fake sessions were opened.
         *
         * @return open count.
         */
        private int openCount() {
            return openCount;
        }
    }

    /**
     * Fake Unix host session used by provider factory tests.
     */
    private static class FakeUnixHostSession implements UnixHostSession {

        private final String id;
        private boolean open = true;

        /**
         * Creates a fake Unix host session.
         *
         * @param id fake physical session identifier.
         */
        private FakeUnixHostSession(String id) {
            this.id = id;
        }

        /**
         * Returns the fake physical session identifier.
         *
         * @param request command request.
         * @return command result containing the fake session identifier.
         */
        @Override
        public CommandResult execute(UnixCommandRequest request) {
            return new CommandResult(0, id, "");
        }

        /**
         * Checks whether the fake session is open.
         *
         * @return {@code true} when open.
         */
        @Override
        public boolean isOpen() {
            return open;
        }

        /**
         * Closes the fake session.
         */
        @Override
        public void close() {
            open = false;
        }
    }
}