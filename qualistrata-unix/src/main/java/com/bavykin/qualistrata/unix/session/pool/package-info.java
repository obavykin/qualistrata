/**
 * Unix session pooling implementation.
 *
 * <p>This package contains reusable Unix session pool support. The pool borrows,
 * releases, invalidates, and closes host/user-bound physical sessions.</p>
 *
 * <p>Pooling is intended to stay transparent for consumers. Consumer code should
 * normally interact with {@link com.bavykin.qualistrata.unix.session.UnixSessionProvider}
 * or {@link com.bavykin.qualistrata.unix.facade.UnixFacade} instead of using
 * pool implementations directly.</p>
 */
package com.bavykin.qualistrata.unix.session.pool;