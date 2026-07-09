package com.bavykin.qualistrata.unix.command;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;
import com.bavykin.qualistrata.core.telemetry.TelemetryEventType;
import com.bavykin.qualistrata.core.telemetry.TelemetrySink;

import java.nio.charset.StandardCharsets;

/**
 * Telemetry-decorating {@link UnixCommandClient}.
 *
 * <p>The decorator measures command execution without changing command
 * semantics. Non-zero exit codes remain normal {@link CommandResult} values.
 * Infrastructure failures are rethrown after a failed duration event is
 * recorded.</p>
 *
 * <p>The decorator intentionally does not record the full command line by
 * default. Commands may contain sensitive arguments, file names, or runtime
 * parameters. Consumer projects can add business-level metrics around their own
 * support APIs when they need a logical operation name such as a TNT utility
 * name.</p>
 */
public class MeasuredUnixCommandClient implements UnixCommandClient {

    /** Generic metric name emitted for command execution duration. */
    public static final String COMMAND_EXECUTION_METRIC = "unix.command.execution.ms";

    private final UnixCommandClient delegate;
    private final TelemetrySink telemetry;

    /**
     * Creates a measured command client.
     *
     * @param delegate command client to decorate.
     * @param telemetry sink receiving duration events.
     */
    public MeasuredUnixCommandClient(
            UnixCommandClient delegate,
            TelemetrySink telemetry
    ) {
        if (delegate == null) {
            throw new UnixCommandException("Measured Unix command delegate must not be null.");
        }

        if (telemetry == null) {
            throw new UnixCommandException("Telemetry sink must not be null.");
        }

        this.delegate = delegate;
        this.telemetry = telemetry;
    }

    /**
     * Executes command and records a generic Unix command duration event.
     *
     * @param request command execution request.
     * @return command execution result.
     */
    @Override
    public CommandResult execute(UnixCommandRequest request) {
        long started = System.nanoTime();

        try {
            CommandResult result = delegate.execute(request);
            recordSuccess(started, result);

            return result;
        } catch (RuntimeException exception) {
            recordFailure(started, exception);

            throw exception;
        }
    }

    private void recordSuccess(long started, CommandResult result) {
        telemetry.record(baseEvent(started, "success")
                .field("exitCode", result.exitCode())
                .field("stdoutBytes", bytes(result.stdout()))
                .field("stderrBytes", bytes(result.stderr()))
                .build());
    }

    private void recordFailure(long started, RuntimeException exception) {
        telemetry.record(baseEvent(started, "failed")
                .field("exceptionType", exception.getClass().getName())
                .build());
    }

    private TelemetryEvent.Builder baseEvent(long started, String status) {
        long durationNanos = System.nanoTime() - started;

        return TelemetryEvent.builder(COMMAND_EXECUTION_METRIC, TelemetryEventType.DURATION)
                .tag("module", "unix")
                .tag("operation", "command")
                .tag("status", status)
                .field("durationNanos", durationNanos)
                .field("durationMillis", durationNanos / 1_000_000.0);
    }

    private long bytes(String value) {
        if (value == null) {
            return 0L;
        }

        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}
