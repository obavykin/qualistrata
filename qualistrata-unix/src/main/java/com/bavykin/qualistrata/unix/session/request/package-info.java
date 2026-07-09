/**
 * Factories for creating Unix session requests from resolved target
 * configuration.
 *
 * <p>This package contains
 * {@link com.bavykin.qualistrata.unix.session.request.UnixHostSessionRequestFactory},
 * which converts logical host aliases from resolved target configuration into
 * {@link com.bavykin.qualistrata.unix.session.UnixHostSessionRequest}
 * instances.</p>
 *
 * <p>The request factory does not load configuration files, merge secrets, or
 * perform project-specific validation. It expects an already resolved and
 * merged target configuration.</p>
 */
package com.bavykin.qualistrata.unix.session.request;