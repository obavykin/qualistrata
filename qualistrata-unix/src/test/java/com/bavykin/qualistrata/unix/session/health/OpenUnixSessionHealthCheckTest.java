package com.bavykin.qualistrata.unix.session.health;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.session.UnixHostSession;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link OpenUnixSessionHealthCheck}.
 */
class OpenUnixSessionHealthCheckTest {

    private final OpenUnixSessionHealthCheck healthCheck = new OpenUnixSessionHealthCheck();

    @Test
    void shouldTreatOpenSessionAsHealthy() {
        assertTrue(healthCheck.isHealthy(new FakeUnixHostSession(true)));
    }

    @Test
    void shouldTreatClosedSessionAsUnhealthy() {
        assertFalse(healthCheck.isHealthy(new FakeUnixHostSession(false)));
    }

    @Test
    void shouldTreatNullSessionAsUnhealthy() {
        assertFalse(healthCheck.isHealthy(null));
    }

    /**
     * Fake session used by health check tests.
     */
    private static class FakeUnixHostSession implements UnixHostSession {

        private final boolean open;

        /**
         * Creates a fake session.
         *
         * @param open value returned from {@link #isOpen()}.
         */
        private FakeUnixHostSession(boolean open) {
            this.open = open;
        }

        /**
         * Executes a command and returns a successful result.
         *
         * @param request command execution request.
         * @return successful command result.
         */
        @Override
        public CommandResult execute(UnixCommandRequest request) {
            return new CommandResult(0, "", "");
        }

        /**
         * @return configured open state.
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
            // Nothing to close in this fake session.
        }
    }
}