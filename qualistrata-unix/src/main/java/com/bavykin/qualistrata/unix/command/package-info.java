/**
 * Unix command execution contracts and local command execution support.
 *
 * <p>This package contains command request validation, command execution
 * contracts, command execution exceptions, and the local command client
 * implementation.</p>
 *
 * <p>Command execution returns
 * {@link com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult}.
 * A non-zero command exit code is considered a normal command result and should
 * not automatically be treated as a framework infrastructure failure.</p>
 */
package com.bavykin.qualistrata.unix.command;