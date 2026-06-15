package com.bavykin.qualistrata.unix.ssh;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SshUnixHostSession}.
 */
class SshUnixHostSessionTest {

    @Test
    void shouldDelegateCommandExecutionToSshConnection() {
        FakeSshConnection connection = new FakeSshConnection();
        SshUnixHostSession session = new SshUnixHostSession(connection);

        UnixCommandRequest request = new UnixCommandRequest(
                "hostname",
                Duration.ofSeconds(5)
        );

        CommandResult result = session.execute(request);

        assertEquals(0, result.exitCode());
        assertEquals("ok", result.stdout());
        assertSame(request, connection.lastRequest());
        assertEquals(1, connection.executeCount());
    }

    @Test
    void shouldDelegateOpenStateToSshConnection() {
        FakeSshConnection connection = new FakeSshConnection();
        SshUnixHostSession session = new SshUnixHostSession(connection);

        assertTrue(session.isOpen());

        connection.close();

        assertFalse(session.isOpen());
    }

    @Test
    void shouldCloseUnderlyingSshConnection() {
        FakeSshConnection connection = new FakeSshConnection();
        SshUnixHostSession session = new SshUnixHostSession(connection);

        session.close();

        assertFalse(connection.isOpen());
        assertEquals(1, connection.closeCount());
    }

    @Test
    void shouldFailWhenConnectionIsNull() {
        SshConnectionException exception = assertThrows(
                SshConnectionException.class,
                () -> new SshUnixHostSession(null)
        );

        assertEquals("SSH connection must not be null.", exception.getMessage());
    }

    /**
     * Fake SSH connection used by session tests.
     */
    private static class FakeSshConnection implements SshConnection {

        private boolean open = true;
        private int executeCount;
        private int closeCount;
        private UnixCommandRequest lastRequest;

        /**
         * Records command execution and returns a successful result.
         *
         * @param request command execution request.
         * @return successful command result.
         */
        @Override
        public CommandResult execute(UnixCommandRequest request) {
            lastRequest = request;
            executeCount++;

            return new CommandResult(0, "ok", "");
        }

        /**
         * @return whether this fake connection is open.
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
            closeCount++;
        }

        /**
         * @return number of command executions.
         */
        private int executeCount() {
            return executeCount;
        }

        /**
         * @return number of close calls.
         */
        private int closeCount() {
            return closeCount;
        }

        /**
         * @return last command request passed to this connection.
         */
        private UnixCommandRequest lastRequest() {
            return lastRequest;
        }
    }
}