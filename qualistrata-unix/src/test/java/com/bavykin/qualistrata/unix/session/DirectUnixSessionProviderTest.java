package com.bavykin.qualistrata.unix.session;

import com.bavykin.qualistrata.core.config.model.HostConfig;
import com.bavykin.qualistrata.core.config.model.UnixPoolConfig;
import com.bavykin.qualistrata.core.config.model.UserConfig;
import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DirectUnixSessionProvider}.
 *
 * <p>The tests verify that the direct provider validates session requests and
 * delegates session creation to the configured factory without introducing
 * pooling behavior.</p>
 */
class DirectUnixSessionProviderTest {

    @Test
    void shouldOpenSessionThroughFactory() {
        FakeUnixHostSession expectedSession = new FakeUnixHostSession();

        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory(expectedSession);
        DirectUnixSessionProvider provider = new DirectUnixSessionProvider(factory);

        UnixHostSessionRequest request = validRequest();

        UnixHostSession actualSession = provider.open(request);

        assertSame(expectedSession, actualSession);
        assertSame(request, factory.lastRequest());
        assertEquals(1, factory.openCount());
    }

    @Test
    void shouldValidateRequestBeforeOpeningSession() {
        RecordingUnixHostSessionFactory factory = new RecordingUnixHostSessionFactory(new FakeUnixHostSession());
        DirectUnixSessionProvider provider = new DirectUnixSessionProvider(factory);

        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> provider.open(new UnixHostSessionRequest(" ", validHost(), "app"))
        );

        assertEquals("Unix host alias must not be blank.", exception.getMessage());
        assertEquals(0, factory.openCount());
    }

    @Test
    void shouldFailWhenFactoryIsNull() {
        UnixSessionException exception = assertThrows(
                UnixSessionException.class,
                () -> new DirectUnixSessionProvider(null)
        );

        assertEquals("Unix host session factory must not be null.", exception.getMessage());
    }

    @Test
    void shouldCloseWithoutFailing() {
        DirectUnixSessionProvider provider = new DirectUnixSessionProvider(
                new RecordingUnixHostSessionFactory(new FakeUnixHostSession())
        );

        provider.close();
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
     * Creates a valid host configuration for direct provider tests.
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

    /**
     * Test factory that records calls and returns a predefined session.
     */
    private static class RecordingUnixHostSessionFactory implements UnixHostSessionFactory {

        private final UnixHostSession session;
        private UnixHostSessionRequest lastRequest;
        private int openCount;

        /**
         * Creates a recording session factory.
         *
         * @param session session returned from {@link #open}.
         */
        private RecordingUnixHostSessionFactory(UnixHostSession session) {
            this.session = session;
        }

        /**
         * Records the request and returns the predefined session.
         *
         * @param request session request.
         * @return predefined session.
         */
        @Override
        public UnixHostSession open(UnixHostSessionRequest request) {
            lastRequest = request;
            openCount++;

            return session;
        }

        /**
         * @return last request passed to {@link #open}.
         */
        private UnixHostSessionRequest lastRequest() {
            return lastRequest;
        }

        /**
         * @return number of times {@link #open} was called.
         */
        private int openCount() {
            return openCount;
        }
    }

    /**
     * Minimal fake session used by direct provider tests.
     */
    private static class FakeUnixHostSession implements UnixHostSession {

        /**
         * Executes a command and returns a simple successful result.
         *
         * @param request command execution request.
         * @return successful command result.
         */
        @Override
        public CommandResult execute(UnixCommandRequest request) {
            return new CommandResult(0, "", "");
        }

        /**
         * @return always {@code true} for this fake session.
         */
        @Override
        public boolean isOpen() {
            return true;
        }

        /**
         * Closes the fake session.
         */
        @Override
        public void close() {
            // Nothing to close in the fake session.
        }
    }
}