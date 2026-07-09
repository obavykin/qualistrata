package com.bavykin.qualistrata.unix.session;

import com.bavykin.qualistrata.unix.interactive.InteractiveCommandResult;
import com.bavykin.qualistrata.unix.interactive.InteractiveCommandScript;

/**
 * Unix host session that supports interactive command execution.
 *
 * <p>This optional extension is used by transports that can keep a command stdin
 * stream open, detect prompts, and send responses. Regular non-interactive
 * sessions continue to use {@link UnixHostSession} only.</p>
 */
public interface InteractiveUnixHostSession extends UnixHostSession {

    /**
     * Executes an interactive command script.
     *
     * @param script interactive command script.
     * @return interactive command result.
     */
    InteractiveCommandResult executeInteractive(InteractiveCommandScript script);
}
