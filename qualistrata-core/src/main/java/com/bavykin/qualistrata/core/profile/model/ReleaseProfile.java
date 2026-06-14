package com.bavykin.qualistrata.core.profile.model;

import java.util.Map;

/**
 * Release-specific environment baseline.
 *
 * <p>Describes expected operating systems and third-party components
 * that must be installed on each logical host for a given release.
 *
 * <p>Loaded from:
 *
 * <pre>
 * release_x.x.yml
 * </pre>
 *
 * Example:
 *
 * <pre>
 * hosts:
 *   app:
 *     unix:
 *       os:
 *         id: rhel
 *         major: 8
 * </pre>
 *
 * @param hosts release expectations grouped by host alias
 */
public record ReleaseProfile(

        Map<String, HostProfile> hosts

) {
}