package com.bavykin.qualistrata.unix.session;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;

/**
 * Open Unix session bound to a specific host and user.
 *
 * <p>A session can represent a real SSH connection, a pooled connection
 * wrapper, a local diagnostic session, or a mock session used in tests.</p>
 *
 * <p>Consumers should treat sessions as {@link AutoCloseable} resources and use
 * try-with-resources. In a pooled implementation, closing a session may return
 * the underlying physical connection to the pool instead of closing it.</p>
 */
public interface UnixHostSession extends AutoCloseable {

    /**
     * Executes a command in this session context.
     *
     * @param request command execution request.
     * @return command result containing exit code, stdout, and stderr.
     */
    CommandResult execute(UnixCommandRequest request);

    /**
     * Checks whether this session is currently open and can be used.
     *
     * @return {@code true} when session is open.
     */
    boolean isOpen();

    /**
     * Closes this session from the consumer perspective.
     *
     * <p>For direct sessions this may close the physical connection. For pooled
     * sessions this may return the physical connection to the pool.</p>
     */
    @Override
    void close();
}