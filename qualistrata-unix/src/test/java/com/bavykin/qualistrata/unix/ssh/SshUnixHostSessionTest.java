package com.bavykin.qualistrata.unix.ssh;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.interactive.InteractiveCommandResult;
import com.bavykin.qualistrata.unix.interactive.InteractiveCommandScript;
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
    void shouldDelegateInteractiveCommandExecutionToSshConnection() {
        FakeSshConnection connection = new FakeSshConnection();
        SshUnixHostSession session = new SshUnixHostSession(connection);

        InteractiveCommandScript script = InteractiveCommandScript.command("setup.sh")
                .respondSecretTo("(?i).*password.*", "secret")
                .build();

        InteractiveCommandResult result = session.executeInteractive(script);

        assertEquals(0, result.exitCode());
        assertEquals("done", result.transcript());
        assertSame(script, connection.lastInteractiveScript());
        assertEquals(1, connection.interactiveExecuteCount());
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
    private static class FakeSshConnection implements InteractiveSshConnection {

        private boolean open = true;
        private int executeCount;
        private int closeCount;
        private UnixCommandRequest lastRequest;
        private InteractiveCommandScript lastInteractiveScript;
        private int interactiveExecuteCount;

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
         * Records interactive command execution and returns a successful result.
         *
         * @param script interactive command script.
         * @return successful interactive command result.
         */
        @Override
        public InteractiveCommandResult executeInteractive(InteractiveCommandScript script) {
            lastInteractiveScript = script;
            interactiveExecuteCount++;

            return new InteractiveCommandResult(
                    0,
                    "done",
                    "",
                    java.util.List.of("(?i).*password.*"),
                    Duration.ofMillis(10)
            );
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

        /**
         * @return last interactive command script passed to this connection.
         */
        private InteractiveCommandScript lastInteractiveScript() {
            return lastInteractiveScript;
        }

        /**
         * @return number of interactive command executions.
         */
        private int interactiveExecuteCount() {
            return interactiveExecuteCount;
        }
    }
}