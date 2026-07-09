/**
 * Generic SSH abstraction layer.
 *
 * <p>This package separates Unix session lifecycle from concrete SSH transport
 * libraries. Higher-level Unix code depends on the generic SSH connection
 * contracts instead of depending directly on JSch or another SSH client.</p>
 *
 * <p>The abstraction allows QualiStrata to support different SSH
 * implementations while keeping the consumer-facing Unix session APIs stable.</p>
 */
package com.bavykin.qualistrata.unix.ssh;