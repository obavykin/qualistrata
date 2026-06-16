package com.bavykin.qualistrata.unix.environment.os;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.core.contracts.unix.dto.OsRelease;
import com.bavykin.qualistrata.core.environment.EnvironmentCheck;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSeverity;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckStatus;
import com.bavykin.qualistrata.unix.facade.UnixFacade;
import com.bavykin.qualistrata.unix.session.UnixSessionException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic Unix operating system baseline environment check.
 *
 * <p>The check reads {@code /etc/os-release} from a configured Unix host and
 * compares detected OS metadata with {@link UnixOperatingSystemProfile}.</p>
 *
 * <p>The check validates only expected profile fields that are explicitly
 * configured. Exact {@code VERSION_ID} validation and major-version validation
 * can be used independently or together. Consuming projects decide how strict
 * the OS baseline should be.</p>
 */
public class UnixOperatingSystemEnvironmentCheck implements EnvironmentCheck {

    private static final String OS_RELEASE_COMMAND = "cat /etc/os-release";

    private final UnixFacade unix;
    private final OsReleaseParser parser;
    private final String id;
    private final String name;
    private final EnvironmentCheckSeverity severity;
    private final String hostAlias;
    private final String userAlias;
    private final UnixOperatingSystemProfile expected;
    private final Duration timeout;

    /**
     * Creates a Unix operating system environment check using the host default
     * user.
     *
     * @param unix Unix facade used to execute the OS detection command.
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity when validation fails.
     * @param hostAlias logical Unix host alias.
     * @param expected expected OS baseline; {@code null} means skipped check.
     * @param timeout command timeout.
     */
    public UnixOperatingSystemEnvironmentCheck(
            UnixFacade unix,
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String hostAlias,
            UnixOperatingSystemProfile expected,
            Duration timeout
    ) {
        this(
                unix,
                new OsReleaseParser(),
                id,
                name,
                severity,
                hostAlias,
                null,
                expected,
                timeout
        );
    }

    /**
     * Creates a Unix operating system environment check using an explicit user
     * alias.
     *
     * @param unix Unix facade used to execute the OS detection command.
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity when validation fails.
     * @param hostAlias logical Unix host alias.
     * @param userAlias optional user alias.
     * @param expected expected OS baseline; {@code null} means skipped check.
     * @param timeout command timeout.
     */
    public UnixOperatingSystemEnvironmentCheck(
            UnixFacade unix,
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String hostAlias,
            String userAlias,
            UnixOperatingSystemProfile expected,
            Duration timeout
    ) {
        this(
                unix,
                new OsReleaseParser(),
                id,
                name,
                severity,
                hostAlias,
                userAlias,
                expected,
                timeout
        );
    }

    /**
     * Creates a Unix operating system environment check.
     *
     * @param unix Unix facade used to execute the OS detection command.
     * @param parser OS release parser.
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity when validation fails.
     * @param hostAlias logical Unix host alias.
     * @param userAlias optional user alias.
     * @param expected expected OS baseline; {@code null} means skipped check.
     * @param timeout command timeout.
     */
    public UnixOperatingSystemEnvironmentCheck(
            UnixFacade unix,
            OsReleaseParser parser,
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String hostAlias,
            String userAlias,
            UnixOperatingSystemProfile expected,
            Duration timeout
    ) {
        if (unix == null) {
            throw new UnixSessionException("Unix facade must not be null.");
        }

        if (parser == null) {
            throw new UnixSessionException("OS release parser must not be null.");
        }

        if (id == null || id.isBlank()) {
            throw new UnixSessionException("Environment check id must not be blank.");
        }

        if (name == null || name.isBlank()) {
            throw new UnixSessionException("Environment check name must not be blank.");
        }

        if (severity == null) {
            throw new UnixSessionException("Environment check severity must not be null.");
        }

        if (hostAlias == null || hostAlias.isBlank()) {
            throw new UnixSessionException("Unix host alias must not be blank.");
        }

        this.unix = unix;
        this.parser = parser;
        this.id = id.trim();
        this.name = name.trim();
        this.severity = severity;
        this.hostAlias = hostAlias.trim();
        this.userAlias = normalizeUserAlias(userAlias);
        this.expected = expected;
        this.timeout = timeout;
    }

    /**
     * Executes the Unix OS baseline check.
     *
     * @return environment check result.
     */
    @Override
    public EnvironmentCheckResult check() {
        long startNanos = System.nanoTime();

        if (expected == null) {
            return skippedBecauseBaselineIsMissing(startNanos);
        }

        try {
            CommandResult commandResult = executeOsReleaseCommand();

            if (commandResult.exitCode() != 0) {
                return failedBecauseCommandFailed(commandResult, startNanos);
            }

            OsRelease actual = parser.parse(commandResult.stdout());
            String validationError = validateOperatingSystem(expected, actual);

            if (validationError == null) {
                return passed(actual, commandResult, startNanos);
            }

            return failedBecauseBaselineMismatch(validationError, actual, commandResult, startNanos);
        } catch (RuntimeException exception) {
            return failedBecauseExceptionWasThrown(exception, startNanos);
        }
    }

    /**
     * Executes {@code cat /etc/os-release} through the Unix facade.
     *
     * @return command execution result.
     */
    private CommandResult executeOsReleaseCommand() {
        if (userAlias == null) {
            return unix.execute(hostAlias, OS_RELEASE_COMMAND, timeout);
        }

        return unix.execute(hostAlias, userAlias, OS_RELEASE_COMMAND, timeout);
    }

    /**
     * Creates skipped result when expected OS baseline is missing.
     *
     * @param startNanos check start timestamp.
     * @return skipped check result.
     */
    private EnvironmentCheckResult skippedBecauseBaselineIsMissing(long startNanos) {
        return new EnvironmentCheckResult(
                id,
                name,
                EnvironmentCheckStatus.SKIPPED,
                severity,
                "Unix operating system baseline is not defined.",
                elapsedSince(startNanos),
                baseDetails(),
                null
        );
    }

    /**
     * Creates passed result.
     *
     * @param actual actual OS release.
     * @param commandResult command result.
     * @param startNanos check start timestamp.
     * @return passed check result.
     */
    private EnvironmentCheckResult passed(
            OsRelease actual,
            CommandResult commandResult,
            long startNanos
    ) {
        return EnvironmentCheckResult.passed(
                id,
                name,
                "Unix operating system baseline matches expected profile.",
                elapsedSince(startNanos),
                details(actual, commandResult)
        );
    }

    /**
     * Creates failed result for non-zero command exit code.
     *
     * @param commandResult command result.
     * @param startNanos check start timestamp.
     * @return failed check result.
     */
    private EnvironmentCheckResult failedBecauseCommandFailed(
            CommandResult commandResult,
            long startNanos
    ) {
        return EnvironmentCheckResult.failed(
                id,
                name,
                severity,
                "Failed to read Unix OS release. Command exit code: " + commandResult.exitCode(),
                elapsedSince(startNanos),
                details(null, commandResult),
                null
        );
    }

    /**
     * Creates failed result for OS baseline mismatch.
     *
     * @param validationError validation error.
     * @param actual actual OS release.
     * @param commandResult command result.
     * @param startNanos check start timestamp.
     * @return failed check result.
     */
    private EnvironmentCheckResult failedBecauseBaselineMismatch(
            String validationError,
            OsRelease actual,
            CommandResult commandResult,
            long startNanos
    ) {
        return EnvironmentCheckResult.failed(
                id,
                name,
                severity,
                validationError,
                elapsedSince(startNanos),
                details(actual, commandResult),
                null
        );
    }

    /**
     * Creates failed result when command execution or parsing throws an
     * unexpected exception.
     *
     * @param exception thrown exception.
     * @param startNanos check start timestamp.
     * @return failed check result.
     */
    private EnvironmentCheckResult failedBecauseExceptionWasThrown(
            RuntimeException exception,
            long startNanos
    ) {
        Map<String, String> details = baseDetails();

        details.put("exceptionType", exception.getClass().getName());

        return EnvironmentCheckResult.failed(
                id,
                name,
                severity,
                "Unix operating system environment check failed with exception: " + exception.getMessage(),
                elapsedSince(startNanos),
                details,
                exception
        );
    }

    /**
     * Validates actual OS release against expected profile.
     *
     * @param expected expected OS profile.
     * @param actual actual OS release.
     * @return validation error, or {@code null} when validation passed.
     */
    private String validateOperatingSystem(
            UnixOperatingSystemProfile expected,
            OsRelease actual
    ) {
        if (expected.id() != null
                && !nullToEmpty(actual.id()).trim().equalsIgnoreCase(expected.id())) {
            return "Unexpected OS id. Actual: "
                    + nullToEmpty(actual.id())
                    + ", expected: "
                    + expected.id();
        }

        if (expected.versionId() != null
                && !nullToEmpty(actual.versionId()).equals(expected.versionId())) {
            return "Unexpected OS VERSION_ID. Actual: "
                    + nullToEmpty(actual.versionId())
                    + ", expected: "
                    + expected.versionId();
        }

        if (expected.major() != null) {
            String actualVersionId = nullToEmpty(actual.versionId());

            if (actualVersionId.isBlank()) {
                return "Actual OS VERSION_ID is not available.";
            }

            int actualMajor = parseMajorVersion(actualVersionId);

            if (actualMajor != expected.major()) {
                return "Unexpected OS major version. Actual: "
                        + actualMajor
                        + ", expected: "
                        + expected.major();
            }
        }

        return null;
    }

    /**
     * Parses major version from {@code VERSION_ID}.
     *
     * @param versionId actual OS {@code VERSION_ID}.
     * @return parsed major version.
     */
    private int parseMajorVersion(String versionId) {
        String majorText = versionId.split("\\.")[0];

        try {
            return Integer.parseInt(majorText);
        } catch (NumberFormatException exception) {
            throw new UnixSessionException(
                    "Unable to parse OS major version from VERSION_ID: " + versionId,
                    exception
            );
        }
    }

    /**
     * Builds common diagnostic details.
     *
     * @return diagnostic details.
     */
    private Map<String, String> baseDetails() {
        Map<String, String> details = new LinkedHashMap<>();

        details.put("hostAlias", hostAlias);
        details.put("userAlias", userAlias == null ? "" : userAlias);
        details.put("command", OS_RELEASE_COMMAND);
        details.put("expectedId", expected == null ? "" : nullToEmpty(expected.id()));
        details.put("expectedVersionId", expected == null ? "" : nullToEmpty(expected.versionId()));
        details.put("expectedMajor", expected == null || expected.major() == null ? "" : String.valueOf(expected.major()));

        return details;
    }

    /**
     * Builds diagnostic details.
     *
     * @param actual actual OS release.
     * @param commandResult command result.
     * @return diagnostic details.
     */
    private Map<String, String> details(
            OsRelease actual,
            CommandResult commandResult
    ) {
        Map<String, String> details = baseDetails();

        details.put("exitCode", commandResult == null ? "" : String.valueOf(commandResult.exitCode()));
        details.put("stdout", commandResult == null ? "" : nullToEmpty(commandResult.stdout()));
        details.put("stderr", commandResult == null ? "" : nullToEmpty(commandResult.stderr()));
        details.put("actualId", actual == null ? "" : nullToEmpty(actual.id()));
        details.put("actualName", actual == null ? "" : nullToEmpty(actual.name()));
        details.put("actualPrettyName", actual == null ? "" : nullToEmpty(actual.prettyName()));
        details.put("actualVersion", actual == null ? "" : nullToEmpty(actual.version()));
        details.put("actualVersionId", actual == null ? "" : nullToEmpty(actual.versionId()));

        return details;
    }

    /**
     * Normalizes optional user alias.
     *
     * @param value optional user alias.
     * @return trimmed user alias, or {@code null} when blank.
     */
    private String normalizeUserAlias(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    /**
     * Converts {@code null} to an empty string.
     *
     * @param value value to normalize.
     * @return original value or empty string.
     */
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * Calculates elapsed duration since the given monotonic timestamp.
     *
     * @param startNanos start timestamp from {@link System#nanoTime()}.
     * @return elapsed duration.
     */
    private Duration elapsedSince(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }
}