package com.bavykin.qualistrata.unix.environment.detector.impl;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionRequest;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionResult;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetector;
import com.bavykin.qualistrata.unix.facade.UnixFacade;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Detects Python package version using {@code importlib.metadata}.
 */
public class PythonPackageVersionDetector extends AbstractUnixComponentDetector implements UnixComponentDetector {

    /**
     * Stable detector id.
     */
    public static final String ID = "python-package";

    private static final Pattern VERSION_PATTERN = Pattern.compile("([0-9A-Za-z][0-9A-Za-z_.+\\-]*)");

    /**
     * Creates a Python package version detector.
     */
    public PythonPackageVersionDetector() {
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
     * Detects Python package version.
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
        String packageName = request.requiredProperty("package");
        String command = "python3 -c \"import importlib.metadata as m; print(m.version('"
                + escapePythonSingleQuoted(packageName)
                + "'))\"";

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
                packageName,
                request,
                command,
                result,
                result.stdout().trim(),
                VERSION_PATTERN
        );
    }

    /**
     * Escapes a value for Python single-quoted string literal inside the command.
     *
     * @param value raw value.
     * @return escaped value.
     */
    private String escapePythonSingleQuoted(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}