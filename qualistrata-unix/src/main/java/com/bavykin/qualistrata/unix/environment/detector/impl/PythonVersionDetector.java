package com.bavykin.qualistrata.unix.environment.detector.impl;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionRequest;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionResult;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetector;
import com.bavykin.qualistrata.unix.facade.UnixFacade;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Detects Python interpreter version using {@code python3 --version}.
 */
public class PythonVersionDetector extends AbstractUnixComponentDetector implements UnixComponentDetector {

    /**
     * Stable detector id.
     */
    public static final String ID = "python";

    private static final String COMMAND = "python3 --version";
    private static final Pattern VERSION_PATTERN = Pattern.compile("Python\\s+(\\d+(?:\\.\\d+)+)");

    /**
     * Creates a Python version detector.
     */
    public PythonVersionDetector() {
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
     * Detects Python version.
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
        CommandResult result = execute(
                unixFacade,
                hostAlias,
                userAlias,
                COMMAND,
                request.shellProfile(),
                timeout
        );

        return resultFromRegex(
                ID,
                "python",
                request,
                COMMAND,
                result,
                result.stdout().isBlank() ? result.stderr() : result.stdout(),
                VERSION_PATTERN
        );
    }
}