package com.bavykin.qualistrata.unix.ssh;

import com.bavykin.qualistrata.unix.interactive.InteractiveCommandResult;
import com.bavykin.qualistrata.unix.interactive.InteractiveCommandScript;

/**
 * SSH connection that supports interactive command execution.
 *
 * <p>This optional extension allows higher-level Unix sessions to execute
 * command scripts that need prompt/response interaction, while the base
 * {@link SshConnection} contract remains focused on non-interactive commands.</p>
 */
public interface InteractiveSshConnection extends SshConnection {

    /**
     * Executes an interactive command script through this SSH connection.
     *
     * @param script interactive command script.
     * @return interactive command result.
     */
    InteractiveCommandResult executeInteractive(InteractiveCommandScript script);
}
