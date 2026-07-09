/**
 * Unix session health-check contracts and default implementations.
 *
 * <p>Health checks are used by pooled session implementations before reusing
 * idle sessions. A session may still exist as an object but no longer be safe
 * to reuse if the underlying transport was closed or broken.</p>
 *
 * <p>Health checks should identify infrastructure-level session usability.
 * They should not treat normal non-zero command exit codes as session failures.</p>
 */
package com.bavykin.qualistrata.unix.session.health;