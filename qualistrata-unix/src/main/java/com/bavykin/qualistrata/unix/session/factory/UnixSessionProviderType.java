package com.bavykin.qualistrata.unix.session.factory;

/**
 * Defines the Unix session provider strategy.
 *
 * <p>The provider type controls whether sessions are opened directly for each
 * request or managed through a reusable session pool.</p>
 */
public enum UnixSessionProviderType {

    /**
     * Opens a new physical Unix session for every request.
     */
    DIRECT,

    /**
     * Reuses physical Unix sessions through a pool.
     */
    POOLED
}