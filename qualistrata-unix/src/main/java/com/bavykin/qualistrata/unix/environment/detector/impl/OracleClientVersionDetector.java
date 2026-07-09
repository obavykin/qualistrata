package com.bavykin.qualistrata.unix.environment.detector.impl;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionRequest;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetectionResult;
import com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetector;
import com.bavykin.qualistrata.unix.facade.UnixFacade;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects Oracle Client version on a Unix host using {@code sqlplus -versionId}.
 *
 * <p>This detector validates the Oracle Client tool available in the Unix
 * command environment. It does not connect to an Oracle database and must not
 * be used as a database server version check.</p>
 */
public class OracleClientVersionDetector extends AbstractUnixComponentDetector implements UnixComponentDetector {

    /**
     * Stable detector id used by consumer-side baseline files.
     */
    public static final String ID = "oracle-client";

    private static final String COMMAND = "sqlplus -versionId";

    private static final Pattern VERSION_LINE_PATTERN =
            Pattern.compile("(?im)^\\s*Version\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*$");

    private static final Pattern VERSION_TOKEN_PATTERN =
            Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+\\.\\d+)");

    /**
     * Creates an Oracle Client version detector.
     */
    public OracleClientVersionDetector() {
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
     * Detects Oracle Client version.
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

        if (result.exitCode() != 0) {
            return new UnixComponentDetectionResult(
                    ID,
                    "oracle-client",
                    request.expectedVersion(),
                    "",
                    COMMAND,
                    result,
                    false,
                    "Unix component detector command failed with exit code: " + result.exitCode()
            );
        }

        String actualVersion = parseVersion(combinedOutput(result));
        boolean matched = request.expectedVersion().equals(actualVersion);

        return new UnixComponentDetectionResult(
                ID,
                "oracle-client",
                request.expectedVersion(),
                actualVersion,
                COMMAND,
                result,
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
     * Parses Oracle Client version from command output.
     *
     * <p>Real SQL*Plus output may contain both a base release value and a more
     * precise patch version value, for example:</p>
     *
     * <pre>
     * SQL*Plus: Release 19.0.0.0.0 - Production
     * Version 19.26.0.0.0
     * </pre>
     *
     * <p>The detector first prefers the explicit {@code Version ...} line. If
     * that line is not present, it falls back to the last version-like token in
     * the output.</p>
     *
     * @param output command output.
     * @return parsed version, or empty string when no version can be found.
     */
    private String parseVersion(String output) {
        String safeOutput = output == null ? "" : output;

        Matcher versionLineMatcher = VERSION_LINE_PATTERN.matcher(safeOutput);

        if (versionLineMatcher.find()) {
            return versionLineMatcher.group(1);
        }

        Matcher tokenMatcher = VERSION_TOKEN_PATTERN.matcher(safeOutput);
        String version = "";

        while (tokenMatcher.find()) {
            version = tokenMatcher.group(1);
        }

        return version;
    }

    /**
     * Combines stdout and stderr because Oracle command-line tools may write
     * version information to either stream depending on installation and shell
     * environment.
     *
     * @param result command result.
     * @return combined command output.
     */
    private String combinedOutput(CommandResult result) {
        return (result.stdout() == null ? "" : result.stdout())
                + System.lineSeparator()
                + (result.stderr() == null ? "" : result.stderr());
    }
}