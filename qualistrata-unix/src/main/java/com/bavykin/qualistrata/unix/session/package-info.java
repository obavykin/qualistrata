/**
 * Core Unix session contracts and provider implementations.
 *
 * <p>This package defines the main session abstractions used by the Unix module:
 * host-bound sessions, session requests, session factories, session providers,
 * and invalidatable session capability.</p>
 *
 * <p>Consumer code should usually depend on
 * {@link com.bavykin.qualistrata.unix.session.UnixSessionProvider} and
 * {@link com.bavykin.qualistrata.unix.session.UnixHostSession}, or use the
 * higher-level {@link com.bavykin.qualistrata.unix.facade.UnixFacade}.</p>
 *
 * <p>Direct and pooled providers are implementation options behind the common
 * provider contract.</p>
 */
package com.bavykin.qualistrata.unix.session;