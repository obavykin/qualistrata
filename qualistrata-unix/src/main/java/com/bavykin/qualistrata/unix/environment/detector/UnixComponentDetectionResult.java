package com.bavykin.qualistrata.unix.environment.detector;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Result of generic Unix component detection.
 *
 * @param detector detector id.
 * @param componentName human-readable component name.
 * @param expectedVersion expected component version.
 * @param actualVersion detected component version.
 * @param command executed command.
 * @param commandResult raw Unix command result.
 * @param matched whether the detected version matched the expected version.
 * @param message diagnostic message.
 */
public record UnixComponentDetectionResult(
        String detector,
        String componentName,
        String expectedVersion,
        String actualVersion,
        String command,
        CommandResult commandResult,
        boolean matched,
        String message
) {

    /**
     * Creates a validated detection result.
     *
     * @param detector detector id.
     * @param componentName component name.
     * @param expectedVersion expected version.
     * @param actualVersion actual version.
     * @param command executed command.
     * @param commandResult command result.
     * @param matched match status.
     * @param message diagnostic message.
     */
    public UnixComponentDetectionResult {
        if (detector == null || detector.isBlank()) {
            throw new UnixComponentDetectorException("Unix component detector id must not be blank.");
        }

        if (componentName == null || componentName.isBlank()) {
            throw new UnixComponentDetectorException("Unix component name must not be blank.");
        }

        if (expectedVersion == null || expectedVersion.isBlank()) {
            throw new UnixComponentDetectorException("Expected Unix component version must not be blank.");
        }

        if (command == null || command.isBlank()) {
            throw new UnixComponentDetectorException("Unix component detector command must not be blank.");
        }

        if (message == null || message.isBlank()) {
            throw new UnixComponentDetectorException("Unix component detection message must not be blank.");
        }

        detector = detector.trim();
        componentName = componentName.trim();
        expectedVersion = expectedVersion.trim();
        actualVersion = actualVersion == null ? "" : actualVersion.trim();
        command = command.trim();
        message = message.trim();
    }

    /**
     * Converts the result into generic report details.
     *
     * @return ordered diagnostic details.
     */
    public Map<String, String> details() {
        Map<String, String> details = new LinkedHashMap<>();

        details.put("detector", detector);
        details.put("componentName", componentName);
        details.put("expectedVersion", expectedVersion);
        details.put("actualVersion", actualVersion);
        details.put("command", command);
        details.put("matched", String.valueOf(matched));

        if (commandResult != null) {
            details.put("exitCode", String.valueOf(commandResult.exitCode()));
            details.put("stdout", nullToEmpty(commandResult.stdout()));
            details.put("stderr", nullToEmpty(commandResult.stderr()));
        }

        return details;
    }

    /**
     * Converts {@code null} to an empty string.
     *
     * @param value raw value.
     * @return normalized value.
     */
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}