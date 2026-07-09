/**
 * Factories for creating Unix session providers.
 *
 * <p>This package contains
 * {@link com.bavykin.qualistrata.unix.session.factory.UnixSessionProviderFactory},
 * which creates direct or pooled Unix session providers and hides low-level
 * provider, pool, SSH session factory, and SSH connection factory wiring.</p>
 *
 * <p>Consumers that do not need direct provider control should usually prefer
 * the higher-level {@link com.bavykin.qualistrata.unix.facade.UnixFacade}.</p>
 */
package com.bavykin.qualistrata.unix.session.factory;