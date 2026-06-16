package com.bavykin.qualistrata.unix.environment;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheck;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSeverity;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.facade.UnixFacade;
import com.bavykin.qualistrata.unix.session.UnixSessionException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic Unix command based environment readiness check.
 *
 * <p>The check executes a command through {@link UnixFacade} and evaluates the
 * command exit code. Exit code {@code 0} is treated as a passed check. Any
 * non-zero exit code is treated as a failed environment check result.</p>
 *
 * <p>Infrastructure-level exceptions are converted into failed check results.
 * This keeps environment readiness reporting consistent and prevents expected
 * environment issues from escaping as raw framework exceptions.</p>
 */
public class UnixCommandEnvironmentCheck implements EnvironmentCheck {

    private final UnixFacade unix;
    private final String id;
    private final String name;
    private final EnvironmentCheckSeverity severity;
    private final String hostAlias;
    private final String userAlias;
    private final UnixCommandRequest commandRequest;

    /**
     * Creates a Unix command environment check using the host default user.
     *
     * @param unix Unix facade used to execute the command.
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity when the command fails.
     * @param hostAlias logical Unix host alias.
     * @param command command line to execute.
     * @param timeout command timeout.
     */
    public UnixCommandEnvironmentCheck(
            UnixFacade unix,
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String hostAlias,
            String command,
            Duration timeout
    ) {
        this(
                unix,
                id,
                name,
                severity,
                hostAlias,
                null,
                new UnixCommandRequest(
                        command,
                        timeout
                )
        );
    }

    /**
     * Creates a Unix command environment check using an explicit user alias.
     *
     * @param unix Unix facade used to execute the command.
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity when the command fails.
     * @param hostAlias logical Unix host alias.
     * @param userAlias optional user alias.
     * @param command command line to execute.
     * @param timeout command timeout.
     */
    public UnixCommandEnvironmentCheck(
            UnixFacade unix,
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String hostAlias,
            String userAlias,
            String command,
            Duration timeout
    ) {
        this(
                unix,
                id,
                name,
                severity,
                hostAlias,
                userAlias,
                new UnixCommandRequest(
                        command,
                        timeout
                )
        );
    }

    /**
     * Creates a Unix command environment check.
     *
     * @param unix Unix facade used to execute the command.
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity when the command fails.
     * @param hostAlias logical Unix host alias.
     * @param userAlias optional user alias.
     * @param commandRequest command execution request.
     */
    public UnixCommandEnvironmentCheck(
            UnixFacade unix,
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String hostAlias,
            String userAlias,
            UnixCommandRequest commandRequest
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

        if (commandRequest == null) {
            throw new UnixSessionException("Unix command request must not be null.");
        }

        this.unix = unix;
        this.id = id.trim();
        this.name = name.trim();
        this.severity = severity;
        this.hostAlias = hostAlias.trim();
        this.userAlias = normalizeUserAlias(userAlias);
        this.commandRequest = commandRequest;
    }

    /**
     * Executes the Unix command environment check.
     *
     * @return environment check result.
     */
    @Override
    public EnvironmentCheckResult check() {
        long startNanos = System.nanoTime();

        try {
            CommandResult result = executeCommand();

            if (result.exitCode() == 0) {
                return EnvironmentCheckResult.passed(
                        id,
                        name,
                        "Unix command environment check passed.",
                        elapsedSince(startNanos),
                        details(result)
                );
            }

            return EnvironmentCheckResult.failed(
                    id,
                    name,
                    severity,
                    "Unix command environment check failed with exit code: " + result.exitCode(),
                    elapsedSince(startNanos),
                    details(result),
                    null
            );
        } catch (RuntimeException exception) {
            return EnvironmentCheckResult.failed(
                    id,
                    name,
                    severity,
                    "Unix command environment check failed with exception: " + exception.getMessage(),
                    elapsedSince(startNanos),
                    Map.of(
                            "hostAlias",
                            hostAlias,
                            "userAlias",
                            userAlias == null ? "" : userAlias,
                            "command",
                            commandRequest.command(),
                            "exceptionType",
                            exception.getClass().getName()
                    ),
                    exception
            );
        }
    }

    /**
     * Executes the configured command through the Unix facade.
     *
     * @return command execution result.
     */
    private CommandResult executeCommand() {
        if (userAlias == null) {
            return unix.execute(hostAlias, commandRequest);
        }

        return unix.execute(hostAlias, userAlias, commandRequest);
    }

    /**
     * Builds diagnostic details for the check result.
     *
     * @param result command execution result.
     * @return diagnostic details.
     */
    private Map<String, String> details(CommandResult result) {
        Map<String, String> details = new LinkedHashMap<>();

        details.put("hostAlias", hostAlias);
        details.put("userAlias", userAlias == null ? "" : userAlias);
        details.put("command", commandRequest.command());
        details.put("exitCode", String.valueOf(result.exitCode()));
        details.put("stdout", nullToEmpty(result.stdout()));
        details.put("stderr", nullToEmpty(result.stderr()));

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