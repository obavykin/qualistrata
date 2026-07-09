/**
 * JSch-based SSH transport implementation.
 *
 * <p>This package contains the concrete SSH implementation backed by the
 * maintained {@code com.github.mwiede:jsch} fork.</p>
 *
 * <p>Classes in this package are infrastructure-level implementation details.
 * Most consumers should use
 * {@link com.bavykin.qualistrata.unix.facade.UnixFacade} or
 * {@link com.bavykin.qualistrata.unix.session.UnixSessionProvider} instead of
 * depending directly on JSch-specific classes.</p>
 */
package com.bavykin.qualistrata.unix.ssh.jsch;