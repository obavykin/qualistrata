package com.bavykin.qualistrata.unix.environment;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheck;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSeverity;
import com.bavykin.qualistrata.unix.command.UnixCommandRequest;
import com.bavykin.qualistrata.unix.command.UnixShellProfileCommandBuilder;
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
 * <p>The check can optionally source a Unix shell profile before executing the
 * command. This is useful for non-interactive SSH sessions where PATH,
 * ORACLE_HOME, LD_LIBRARY_PATH, or other environment variables are initialized
 * by a project/user-specific profile.</p>
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
    private final String shellProfile;
    private final UnixCommandRequest commandRequest;
    private final UnixCommandRequest effectiveCommandRequest;

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
                null,
                new UnixCommandRequest(
                        command,
                        timeout
                )
        );
    }

    /**
     * Creates a Unix command environment check.
     *
     * <p>This constructor is kept for backward compatibility with existing
     * consumers that build {@link UnixCommandRequest} directly and do not use shell
     * profile wrapping.</p>
     *
     * @param unix Unix facade used to execute the command.
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity when the command fails.
     * @param hostAlias logical Unix host alias.
     * @param userAlias optional user alias.
     * @param commandRequest command execution request.
     */
    @SuppressWarnings("unused")
    public UnixCommandEnvironmentCheck(
            UnixFacade unix,
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String hostAlias,
            String userAlias,
            UnixCommandRequest commandRequest
    ) {
        this(
                unix,
                id,
                name,
                severity,
                hostAlias,
                userAlias,
                null,
                commandRequest
        );
    }

    /**
     * Creates a shell-profile-aware Unix command environment check.
     *
     * <p>This constructor is intentionally the only profile-aware constructor
     * for generic command checks. A shorter overload with raw {@code command}
     * and {@code shellProfile} would conflict with the existing explicit-user
     * constructor because Java method signatures do not include parameter
     * names.</p>
     *
     * @param unix Unix facade used to execute the command.
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity check severity when the command fails.
     * @param hostAlias logical Unix host alias.
     * @param userAlias optional user alias.
     * @param shellProfile optional Unix shell profile to source before command
     *                     execution.
     * @param commandRequest command execution request.
     */
    public UnixCommandEnvironmentCheck(
            UnixFacade unix,
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            String hostAlias,
            String userAlias,
            String shellProfile,
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
        this.userAlias = normalizeOptionalValue(userAlias);
        this.shellProfile = normalizeOptionalValue(shellProfile);
        this.commandRequest = commandRequest;
        this.effectiveCommandRequest = effectiveCommandRequest(commandRequest, this.shellProfile);
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
            Map<String, String> details = baseDetails();

            details.put("exceptionType", exception.getClass().getName());

            return EnvironmentCheckResult.failed(
                    id,
                    name,
                    severity,
                    "Unix command environment check failed with exception: " + exception.getMessage(),
                    elapsedSince(startNanos),
                    details,
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
            return unix.execute(hostAlias, effectiveCommandRequest);
        }

        return unix.execute(hostAlias, userAlias, effectiveCommandRequest);
    }

    /**
     * Builds diagnostic details for the check result.
     *
     * @param result command execution result.
     * @return diagnostic details.
     */
    private Map<String, String> details(CommandResult result) {
        Map<String, String> details = baseDetails();

        details.put("exitCode", String.valueOf(result.exitCode()));
        details.put("stdout", nullToEmpty(result.stdout()));
        details.put("stderr", nullToEmpty(result.stderr()));

        return details;
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
        details.put("shellProfile", shellProfile == null ? "" : shellProfile);
        details.put("command", effectiveCommandRequest.command());
        details.put("rawCommand", commandRequest.command());

        return details;
    }

    /**
     * Builds an effective command request with optional shell profile wrapping.
     *
     * @param commandRequest original command request.
     * @param shellProfile optional shell profile.
     * @return effective command request.
     */
    private UnixCommandRequest effectiveCommandRequest(
            UnixCommandRequest commandRequest,
            String shellProfile
    ) {
        UnixShellProfileCommandBuilder builder = new UnixShellProfileCommandBuilder();

        return new UnixCommandRequest(
                builder.build(commandRequest.command(), shellProfile),
                commandRequest.timeout()
        );
    }

    /**
     * Normalizes optional string value.
     *
     * @param value optional value.
     * @return trimmed value, or {@code null} when blank.
     */
    private String normalizeOptionalValue(String value) {
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