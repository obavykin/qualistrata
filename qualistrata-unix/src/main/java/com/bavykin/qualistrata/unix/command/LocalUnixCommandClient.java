package com.bavykin.qualistrata.unix.command;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Local implementation of {@link UnixCommandClient} based on {@link ProcessBuilder}.
 *
 * <p>This client executes commands on the machine where the tests are running.
 * It is primarily useful as a reference implementation, for local diagnostics,
 * and for testing the command execution contract without SSH infrastructure.</p>
 *
 * <p>Although the module is named {@code qualistrata-unix}, this implementation
 * uses a small shell adapter to support local execution on both Unix-like
 * systems and Windows development machines:</p>
 *
 * <ul>
 *     <li>Unix/Linux/macOS: {@code /bin/sh -c command}</li>
 *     <li>Windows: {@code cmd.exe /c command}</li>
 * </ul>
 *
 * <p>A non-zero process exit code is returned as a normal {@link CommandResult}.
 * Infrastructure failures, invalid requests, interrupted execution, and
 * timeouts are reported as {@link UnixCommandException}.</p>
 */
public class LocalUnixCommandClient implements UnixCommandClient {

    private final UnixCommandRequestValidator requestValidator;

    /**
     * Creates a local Unix command client with the default request validator.
     */
    public LocalUnixCommandClient() {
        this(new UnixCommandRequestValidator());
    }

    /**
     * Creates a local Unix command client with an explicitly provided request
     * validator.
     *
     * <p>This constructor keeps the class testable and allows future extension
     * without relying on static global state.</p>
     *
     * @param requestValidator validator used before command execution.
     */
    LocalUnixCommandClient(UnixCommandRequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }

    /**
     * Executes a command on the local machine.
     *
     * @param request command execution request.
     * @return command result containing exit code, stdout, and stderr.
     * @throws UnixCommandException if the request is invalid, the process cannot
     *                              be started, the execution is interrupted, or
     *                              the timeout expires.
     */
    @Override
    public CommandResult execute(UnixCommandRequest request) {
        requestValidator.validate(request);

        Process process = startProcess(request.command());

        waitForProcess(process, request.timeout());

        return readResult(process);
    }

    /**
     * Starts a local process through an operating-system-specific shell.
     *
     * @param command command line to execute.
     * @return started process.
     */
    private Process startProcess(String command) {
        try {
            return new ProcessBuilder(shellCommand(command)).start();
        } catch (IOException exception) {
            throw new UnixCommandException("Failed to start local Unix command: " + command, exception);
        }
    }

    /**
     * Waits for process completion and enforces timeout when it is configured.
     *
     * @param process started process.
     * @param timeout optional execution timeout.
     */
    private void waitForProcess(Process process, Duration timeout) {
        try {
            boolean completed = timeout == null
                    ? waitWithoutTimeout(process)
                    : process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);

            if (!completed) {
                process.destroyForcibly();

                throw new UnixCommandException("Local Unix command timed out after: " + timeout);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            process.destroyForcibly();

            throw new UnixCommandException("Local Unix command execution was interrupted.", exception);
        }
    }

    /**
     * Waits for the process without timeout.
     *
     * @param process started process.
     * @return always {@code true} when process completion is observed.
     * @throws InterruptedException if the current thread is interrupted.
     */
    private boolean waitWithoutTimeout(Process process) throws InterruptedException {
        process.waitFor();

        return true;
    }

    /**
     * Reads process exit code, stdout, and stderr after completion.
     *
     * @param process completed process.
     * @return command result.
     */
    private CommandResult readResult(Process process) {
        try {
            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

            return new CommandResult(process.exitValue(), stdout, stderr);
        } catch (IOException exception) {
            throw new UnixCommandException("Failed to read local Unix command output.", exception);
        }
    }

    /**
     * Builds an operating-system-specific shell command.
     *
     * @param command command line to execute.
     * @return shell command arguments for {@link ProcessBuilder}.
     */
    private List<String> shellCommand(String command) {
        if (isWindows()) {
            return List.of("cmd.exe", "/c", command);
        }

        return List.of("/bin/sh", "-c", command);
    }

    /**
     * Detects whether the current JVM is running on Windows.
     *
     * @return {@code true} when current operating system looks like Windows.
     */
    private boolean isWindows() {
        String osName = System.getProperty("os.name");

        return osName != null && osName.toLowerCase().contains("win");
    }
}