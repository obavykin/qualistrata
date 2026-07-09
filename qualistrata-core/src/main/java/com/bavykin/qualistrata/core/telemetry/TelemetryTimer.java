package com.bavykin.qualistrata.core.telemetry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Small helper for measuring operation duration and recording it as telemetry.
 *
 * <p>The timer records both successful and failed executions. Failures are
 * rethrown unchanged after the duration event is recorded with status
 * {@code failed} and the exception type as a diagnostic field.</p>
 */
public final class TelemetryTimer {

    private final TelemetrySink sink;
    private final String name;
    private final Map<String, String> tags = new LinkedHashMap<>();
    private final Map<String, Object> fields = new LinkedHashMap<>();

    /**
     * Creates a timer.
     *
     * @param sink target telemetry sink.
     * @param name duration metric name.
     */
    public TelemetryTimer(TelemetrySink sink, String name) {
        if (sink == null) {
            throw new IllegalArgumentException("Telemetry sink must not be null.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Telemetry timer name must not be blank.");
        }

        this.sink = sink;
        this.name = name;
    }

    /**
     * Adds a timer tag.
     *
     * @param key tag key.
     * @param value tag value.
     * @return this timer.
     */
    public TelemetryTimer tag(String key, String value) {
        if (value != null) {
            tags.put(requireKey(key, "tag"), value);
        }

        return this;
    }

    /**
     * Adds a timer field.
     *
     * @param key field key.
     * @param value field value.
     * @return this timer.
     */
    public TelemetryTimer field(String key, Object value) {
        if (value != null) {
            fields.put(requireKey(key, "field"), value);
        }

        return this;
    }

    /**
     * Measures a supplier and returns its result.
     *
     * @param operation operation to execute.
     * @param <T> result type.
     * @return operation result.
     */
    public <T> T measure(Supplier<T> operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Measured operation must not be null.");
        }

        long started = System.nanoTime();

        try {
            T result = operation.get();
            record(started, "success", null);

            return result;
        } catch (RuntimeException | Error exception) {
            record(started, "failed", exception);

            throw exception;
        }
    }

    /**
     * Measures a runnable operation.
     *
     * @param operation operation to execute.
     */
    public void measure(Runnable operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Measured operation must not be null.");
        }

        measure(() -> {
            operation.run();
            return null;
        });
    }

    private void record(long started, String status, Throwable exception) {
        long durationNanos = System.nanoTime() - started;
        TelemetryEvent.Builder builder = TelemetryEvent.builder(name, TelemetryEventType.DURATION)
                .field("durationNanos", durationNanos)
                .field("durationMillis", durationNanos / 1_000_000.0)
                .tag("status", status);

        tags.forEach(builder::tag);
        fields.forEach(builder::field);

        if (exception != null) {
            builder.field("exceptionType", exception.getClass().getName());
        }

        sink.record(builder.build());
    }

    private String requireKey(String key, String type) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Telemetry " + type + " key must not be blank.");
        }

        return key;
    }
}
