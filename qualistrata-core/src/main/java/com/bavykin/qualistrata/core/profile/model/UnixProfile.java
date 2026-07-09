package com.bavykin.qualistrata.core.profile.model;

import java.util.Map;

/**
 * Unix environment baseline for a host.
 *
 * @param profile optional environment profile script to source before detector commands
 * @param os expected operating system information
 * @param components expected third-party components installed on the host
 */
public record UnixProfile(
        String profile,
        OsProfile os,
        Map<String, ComponentProfile> components
) {
}