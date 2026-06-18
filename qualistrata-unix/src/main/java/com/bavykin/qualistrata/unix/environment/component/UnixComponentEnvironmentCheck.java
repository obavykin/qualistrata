package com.bavykin.qualistrata.unix.environment.component;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheck;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSeverity;
import com.bavykin.qualistrata.core.environment.matching.EnvironmentValueMatchResult;
import com.bavykin.qualistrata.core.environment.matching.EnvironmentValueMatcher;
import com.bavykin.qualistrata.unix.facade.UnixFacade;
import com.bavykin.qualistrata.unix.session.UnixSessionException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic Unix command based component environment check.
 *
 * <p>The check executes a command through {@link UnixFacade}, extracts an
 * actual component value from command output, and validates it using
 * {@link EnvironmentValueMatcher}.</p>
 *
 * <p>This class is intentionally component-neutral. It does not know about
 * Python, Java, Apache, Oracle, or any project-specific component. A consuming
 * project defines the command and matcher that represent its expected
 * component baseline.</p>
 */
public class UnixComponentEnvironmentCheck implements EnvironmentCheck {

    private final UnixFacade unix;
    private final String id;
    private final String name;
    private final EnvironmentCheckSeverity severity;
    private final String hostAlias;
    private final String userAlias;
    private final String componentName;
    private final String command;
    private final Duration timeout;
    private final EnvironmentValueMatcher matcher;
    private final UnixComponentOutputSource outputSource;

    /**
     * Creates a Unix component check using the host default user and standard
     * output as the actual value.
     *
     * @param unix Unix facade used to execute the command.
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity when validation fails.
     * @param hostAlias logical Unix host alias.
     * @param componentName component name for diagnostics.
     * @param command component detection command.
     * @param timeout command timeout.
     * @param matcher value matcher used to validate detected output.
     */
    public UnixComponentEnvironmentCheck(
            UnixFacade unix,
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String hostAlias,
            String componentName,
            String command,
            Duration timeout,
            EnvironmentValueMatcher matcher
    ) {
        this(
                unix,
                id,
                name,
                severity,
                hostAlias,
                null,
                componentName,
                command,
                timeout,
                matcher,
                UnixComponentOutputSource.STDOUT
        );
    }

    /**
     * Creates a Unix component check using an explicit user alias.
     *
     * @param unix Unix facade used to execute the command.
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity when validation fails.
     * @param hostAlias logical Unix host alias.
     * @param userAlias optional user alias.
     * @param componentName component name for diagnostics.
     * @param command component detection command.
     * @param timeout command timeout.
     * @param matcher value matcher used to validate detected output.
     * @param outputSource command output stream used as actual value.
     */
    public UnixComponentEnvironmentCheck(
            UnixFacade unix,
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String hostAlias,
            String userAlias,
            String componentName,
            String command,
            Duration timeout,
            EnvironmentValueMatcher matcher,
            UnixComponentOutputSource outputSource
    ) {
        if (unix == null) {
            throw new UnixSessionException("Unix facade must not be null.");
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

        if (componentName == null || componentName.isBlank()) {
            throw new UnixSessionException("Unix component name must not be blank.");
        }

        if (command == null || command.isBlank()) {
            throw new UnixSessionException("Unix component detection command must not be blank.");
        }

        if (matcher == null) {
            throw new UnixSessionException("Environment value matcher must not be null.");
        }

        if (outputSource == null) {
            throw new UnixSessionException("Unix component output source must not be null.");
        }

        this.unix = unix;
        this.id = id.trim();
        this.name = name.trim();
        this.severity = severity;
        this.hostAlias = hostAlias.trim();
        this.userAlias = normalizeUserAlias(userAlias);
        this.componentName = componentName.trim();
        this.command = command.trim();
        this.timeout = timeout;
        this.matcher = matcher;
        this.outputSource = outputSource;
    }

    /**
     * Executes the Unix component environment check.
     *
     * @return environment check result.
     */
    @Override
    public EnvironmentCheckResult check() {
        long startNanos = System.nanoTime();

        try {
            CommandResult commandResult = executeCommand();

            if (commandResult.exitCode() != 0) {
                return failedBecauseCommandFailed(commandResult, startNanos);
            }

            String actualValue = actualValue(commandResult);
            EnvironmentValueMatchResult matchResult = matcher.match(actualValue);

            if (matchResult.matched()) {
                return passed(commandResult, matchResult, startNanos);
            }

            return failedBecauseValueDidNotMatch(commandResult, matchResult, startNanos);
        } catch (RuntimeException exception) {
            return failedBecauseExceptionWasThrown(exception, startNanos);
        }
    }

    /**
     * Executes the configured component detection command.
     *
     * @return command execution result.
     */
    private CommandResult executeCommand() {
        if (userAlias == null) {
            return unix.execute(hostAlias, command, timeout);
        }

        return unix.execute(hostAlias, userAlias, command, timeout);
    }

    /**
     * Creates a passed check result.
     *
     * @param commandResult command execution result.
     * @param matchResult value match result.
     * @param startNanos check start timestamp.
     * @return passed check result.
     */
    private EnvironmentCheckResult passed(
            CommandResult commandResult,
            EnvironmentValueMatchResult matchResult,
            long startNanos
    ) {
        return EnvironmentCheckResult.passed(
                id,
                name,
                "Unix component baseline matches expected value.",
                elapsedSince(startNanos),
                details(commandResult, matchResult)
        );
    }

    /**
     * Creates a failed result for non-zero command exit code.
     *
     * @param commandResult command execution result.
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
                "Unix component detection command failed with exit code: " + commandResult.exitCode(),
                elapsedSince(startNanos),
                details(commandResult, null),
                null
        );
    }

    /**
     * Creates a failed result when the actual value does not match the expected
     * baseline.
     *
     * @param commandResult command execution result.
     * @param matchResult value match result.
     * @param startNanos check start timestamp.
     * @return failed check result.
     */
    private EnvironmentCheckResult failedBecauseValueDidNotMatch(
            CommandResult commandResult,
            EnvironmentValueMatchResult matchResult,
            long startNanos
    ) {
        return EnvironmentCheckResult.failed(
                id,
                name,
                severity,
                matchResult.message(),
                elapsedSince(startNanos),
                details(commandResult, matchResult),
                null
        );
    }

    /**
     * Creates a failed result when command execution or matching throws an
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
                "Unix component environment check failed with exception: " + exception.getMessage(),
                elapsedSince(startNanos),
                details,
                exception
        );
    }

    /**
     * Extracts actual component value from command result.
     *
     * @param commandResult command execution result.
     * @return actual value used by matcher.
     */
    private String actualValue(CommandResult commandResult) {
        String stdout = nullToEmpty(commandResult.stdout());
        String stderr = nullToEmpty(commandResult.stderr());

        return switch (outputSource) {
            case STDOUT -> stdout;
            case STDERR -> stderr;
            case STDOUT_THEN_STDERR -> stdout.isBlank() ? stderr : stdout;
            case COMBINED -> combine(stdout, stderr);
        };
    }

    /**
     * Combines standard output and standard error into a single value.
     *
     * @param stdout standard output.
     * @param stderr standard error.
     * @return combined output.
     */
    private String combine(
            String stdout,
            String stderr
    ) {
        if (stdout.isBlank()) {
            return stderr;
        }

        if (stderr.isBlank()) {
            return stdout;
        }

        return stdout + System.lineSeparator() + stderr;
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
        details.put("componentName", componentName);
        details.put("command", command);
        details.put("outputSource", outputSource.name());

        return details;
    }

    /**
     * Builds diagnostic details.
     *
     * @param commandResult command execution result.
     * @param matchResult optional match result.
     * @return diagnostic details.
     */
    private Map<String, String> details(
            CommandResult commandResult,
            EnvironmentValueMatchResult matchResult
    ) {
        Map<String, String> details = baseDetails();

        details.put("exitCode", commandResult == null ? "" : String.valueOf(commandResult.exitCode()));
        details.put("stdout", commandResult == null ? "" : nullToEmpty(commandResult.stdout()));
        details.put("stderr", commandResult == null ? "" : nullToEmpty(commandResult.stderr()));

        if (matchResult != null) {
            details.put("expected", matchResult.expected());
            details.put("actual", matchResult.actual());
            details.put("matched", String.valueOf(matchResult.matched()));
        }

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