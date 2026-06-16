package com.bavykin.qualistrata.unix.environment.os;

/**
 * Expected Unix operating system baseline.
 *
 * <p>The profile contains only generic Unix OS expectations and is not tied to
 * any specific delivery project, release naming convention, or product
 * topology.</p>
 *
 * <p>Only non-null fields are validated. This allows consuming projects to
 * choose between broad validation, such as OS id and major version, and strict
 * validation, such as exact {@code VERSION_ID} from {@code /etc/os-release}.</p>
 *
 * @param id expected operating system id, for example {@code rhel} or {@code ubuntu}.
 * @param major expected major version parsed from {@code VERSION_ID}.
 * @param versionId expected exact {@code VERSION_ID}.
 */
public record UnixOperatingSystemProfile(
        String id,
        Integer major,
        String versionId
) {

    /**
     * Creates a validated Unix operating system profile.
     *
     * @param id expected operating system id.
     * @param major expected major version.
     * @param versionId expected exact {@code VERSION_ID}.
     */
    public UnixOperatingSystemProfile {
        id = normalize(id);
        versionId = normalize(versionId);

        if (major != null && major <= 0) {
            throw new IllegalArgumentException("Unix operating system major version must be positive.");
        }
    }

    /**
     * Normalizes optional text values.
     *
     * @param value value to normalize.
     * @return trimmed value, or {@code null} when blank.
     */
    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}