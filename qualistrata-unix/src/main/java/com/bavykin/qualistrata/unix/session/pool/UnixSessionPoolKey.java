package com.bavykin.qualistrata.unix.session.pool;

import com.bavykin.qualistrata.unix.session.UnixHostSessionRequest;

/**
 * Key used to group reusable Unix sessions in the pool.
 *
 * <p>The key is intentionally based on stable connection identity fields:
 * logical host alias, physical host address, port, and resolved user alias.
 * This prevents sessions for different hosts or users from being reused
 * incorrectly.</p>
 *
 * @param hostAlias logical host alias.
 * @param host physical host address.
 * @param port host port.
 * @param userAlias resolved logical user alias.
 */
record UnixSessionPoolKey(
        String hostAlias,
        String host,
        Integer port,
        String userAlias
) {

    /**
     * Creates a pool key from a validated session request.
     *
     * @param request validated session request.
     * @return pool key.
     */
    static UnixSessionPoolKey from(UnixHostSessionRequest request) {
        return new UnixSessionPoolKey(
                request.hostAlias().trim(),
                request.host().host().trim(),
                request.host().port(),
                request.resolvedUserAlias()
        );
    }
}