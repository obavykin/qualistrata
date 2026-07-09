package com.bavykin.qualistrata.unix.environment.detector;

import java.util.Map;

/**
 * Request for generic Unix component detection.
 *
 * @param detector stable detector id, for example {@code python-package}.
 * @param expectedVersion expected component version.
 * @param shellProfile optional Unix shell profile that should be sourced before
 *                     running the detector command.
 * @param properties detector-specific generic properties.
 */
public record UnixComponentDetectionRequest(
        String detector,
        String expectedVersion,
        String shellProfile,
        Map<String, String> properties
) {

    /**
     * Creates a validated detection request.
     *
     * @param detector stable detector id.
     * @param expectedVersion expected component version.
     * @param shellProfile optional shell profile.
     * @param properties detector-specific properties.
     */
    public UnixComponentDetectionRequest {
        if (detector == null || detector.isBlank()) {
            throw new UnixComponentDetectorException("Unix component detector id must not be blank.");
        }

        if (expectedVersion == null || expectedVersion.isBlank()) {
            throw new UnixComponentDetectorException("Expected Unix component version must not be blank.");
        }

        detector = detector.trim();
        expectedVersion = expectedVersion.trim();
        shellProfile = normalizeOptional(shellProfile);
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    /**
     * Returns a required detector property.
     *
     * @param name property name.
     * @return property value.
     * @throws UnixComponentDetectorException if the property is missing or blank.
     */
    public String requiredProperty(String name) {
        String value = properties.get(name);

        if (value == null || value.isBlank()) {
            throw new UnixComponentDetectorException(
                    "Required Unix component detector property is missing or blank: " + name
            );
        }

        return value.trim();
    }

    /**
     * Normalizes optional string value.
     *
     * @param value raw value.
     * @return trimmed value, or {@code null} when blank.
     */
    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}