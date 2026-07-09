package com.bavykin.qualistrata.unix.environment.detector.impl;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionRequest;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionResult;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetector;
import com.bavykin.qualistrata.unix.facade.UnixFacade;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Detects Apache HTTP Server version.
 *
 * <p>The detector tries {@code httpd -v} first and then falls back to
 * {@code apache2 -v}, because command names differ across Unix distributions.</p>
 */
public class ApacheVersionDetector extends AbstractUnixComponentDetector implements UnixComponentDetector {

    /**
     * Stable detector id.
     */
    public static final String ID = "apache";

    private static final String HTTPD_COMMAND = "httpd -v";
    private static final String APACHE2_COMMAND = "apache2 -v";
    private static final Pattern VERSION_PATTERN = Pattern.compile("Apache/(\\d+(?:\\.\\d+)+)");

    /**
     * Creates an Apache version detector.
     */
    public ApacheVersionDetector() {
    }

    /**
     * Returns detector id.
     *
     * @return detector id.
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Detects Apache version.
     *
     * @param unixFacade Unix facade.
     * @param hostAlias host alias.
     * @param userAlias optional user alias.
     * @param request detection request.
     * @param timeout command timeout.
     * @return detection result.
     */
    @Override
    public UnixComponentDetectionResult detect(
            UnixFacade unixFacade,
            String hostAlias,
            String userAlias,
            UnixComponentDetectionRequest request,
            Duration timeout
    ) {
        String command = HTTPD_COMMAND + " || " + APACHE2_COMMAND;

        CommandResult result = execute(
                unixFacade,
                hostAlias,
                userAlias,
                command,
                request.shellProfile(),
                timeout
        );

        return resultFromRegex(
                ID,
                "apache",
                request,
                command,
                result,
                combinedOutput(result),
                VERSION_PATTERN
        );
    }

    /**
     * Combines stdout and stderr because different Apache binaries may write
     * version information to different streams.
     *
     * @param result command result.
     * @return combined output.
     */
    private String combinedOutput(CommandResult result) {
        return (result.stdout() == null ? "" : result.stdout())
                + System.lineSeparator()
                + (result.stderr() == null ? "" : result.stderr());
    }
}