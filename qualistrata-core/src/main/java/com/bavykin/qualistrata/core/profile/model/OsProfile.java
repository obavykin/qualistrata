package com.bavykin.qualistrata.core.profile.model;

/**
 * Expected operating system baseline.
 *
 * <p>Used to validate that a Unix host is running the expected
 * operating system and versionId for a particular release.
 *
 * <p>Example:
 *
 * <pre>
 * os:
 *   id: rhel
 *   major: 8
 *   versionId: "8.10"
 * </pre>
 *
 * @param id operating system identifier
 * @param major expected major versionId
 * @param versionId expected VERSION_ID from /etc/os-release
 */
public record OsProfile(

        String id,

        Integer major,

        String versionId

) {
}