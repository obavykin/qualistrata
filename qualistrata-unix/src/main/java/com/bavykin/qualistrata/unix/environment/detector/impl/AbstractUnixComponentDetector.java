package com.bavykin.qualistrata.unix.environment.detector.impl;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionResult;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectorException;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionRequest;
import com.bavykin.qualistrata.unix.command.UnixShellProfileCommandBuilder;
import com.bavykin.qualistrata.unix.facade.UnixFacade;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for command-based Unix component detectors.
 */
abstract class AbstractUnixComponentDetector {

    private final UnixShellProfileCommandBuilder commandBuilder = new UnixShellProfileCommandBuilder();

    /**
     * Executes a detector command using the provided Unix facade.
     *
     * @param unixFacade Unix facade.
     * @param hostAlias host alias.
     * @param userAlias optional user alias.
     * @param command command to execute.
     * @param shellProfile optional shell profile.
     * @param timeout command timeout.
     * @return command result.
     */
    protected CommandResult execute(
            UnixFacade unixFacade,
            String hostAlias,
            String userAlias,
            String command,
            String shellProfile,
            Duration timeout
    ) {
        if (unixFacade == null) {
            throw new UnixComponentDetectorException("Unix facade must not be null.");
        }

        if (hostAlias == null || hostAlias.isBlank()) {
            throw new UnixComponentDetectorException("Unix host alias must not be blank.");
        }

        String effectiveCommand = commandBuilder.build(command, shellProfile);

        if (userAlias == null || userAlias.isBlank()) {
            return unixFacade.execute(hostAlias.trim(), effectiveCommand, timeout);
        }

        return unixFacade.execute(hostAlias.trim(), userAlias.trim(), effectiveCommand, timeout);
    }

    /**
     * Creates a standard detection result from command output and a version
     * regex.
     *
     * @param detector detector id.
     * @param componentName component name.
     * @param request original detection request.
     * @param command executed command.
     * @param commandResult command result.
     * @param output output used for version parsing.
     * @param versionPattern regex with the first group containing version.
     * @return detection result.
     */
    protected UnixComponentDetectionResult resultFromRegex(
            String detector,
            String componentName,
            UnixComponentDetectionRequest request,
            String command,
            CommandResult commandResult,
            String output,
            Pattern versionPattern
    ) {
        if (commandResult.exitCode() != 0) {
            return new UnixComponentDetectionResult(
                    detector,
                    componentName,
                    request.expectedVersion(),
                    "",
                    command,
                    commandResult,
                    false,
                    "Unix component detector command failed with exit code: " + commandResult.exitCode()
            );
        }

        String actualVersion = firstGroup(output, versionPattern);
        boolean matched = request.expectedVersion().equals(actualVersion);

        return new UnixComponentDetectionResult(
                detector,
                componentName,
                request.expectedVersion(),
                actualVersion,
                command,
                commandResult,
                matched,
                matched
                        ? "Unix component version matches expected baseline."
                        : "Unix component version mismatch. Actual: "
                          + actualVersion
                          + ", expected: "
                          + request.expectedVersion()
        );
    }

    /**
     * Extracts the first regex group from output.
     *
     * @param output command output.
     * @param pattern regex pattern.
     * @return extracted group, or empty string when not found.
     */
    private String firstGroup(String output, Pattern pattern) {
        Matcher matcher = pattern.matcher(output == null ? "" : output);

        if (!matcher.find()) {
            return "";
        }

        return matcher.group(1);
    }
}