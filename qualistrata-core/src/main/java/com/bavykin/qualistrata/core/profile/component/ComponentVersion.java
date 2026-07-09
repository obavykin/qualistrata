package com.bavykin.qualistrata.core.profile.component;

/**
 * Actual detected versionId of an installed component.
 *
 * <p>Returned by component detectors and compared with the expected
 * versionId defined in the release profile.
 *
 * @param name component name or alias
 * @param detector detector identifier used to detect the component
 * @param version parsed component versionId
 * @param rawOutput original command output used for parsing
 */
public record ComponentVersion(
        String name,
        String detector,
        String version,
        String rawOutput
) {
}