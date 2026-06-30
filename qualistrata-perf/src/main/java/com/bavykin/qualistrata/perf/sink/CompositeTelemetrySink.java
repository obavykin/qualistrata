package com.bavykin.qualistrata.perf.sink;

import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;
import com.bavykin.qualistrata.core.telemetry.TelemetrySink;

import java.util.List;

/**
 * Fan-out telemetry sink that sends each event to multiple destinations.
 *
 * <p>This is the first building block for local JSON/HTML reporting plus future
 * external publishers such as InfluxDB. It does not hide publishing failures;
 * wrap it in {@link SafeTelemetrySink} when normal functional execution must be
 * protected from telemetry backend problems.</p>
 */
public class CompositeTelemetrySink implements TelemetrySink {

    private final List<TelemetrySink> delegates;

    /**
     * Creates a composite sink.
     *
     * @param delegates telemetry destinations.
     */
    public CompositeTelemetrySink(List<TelemetrySink> delegates) {
        if (delegates == null || delegates.isEmpty()) {
            throw new IllegalArgumentException("Telemetry delegates must not be empty.");
        }

        if (delegates.stream().anyMatch(delegate -> delegate == null)) {
            throw new IllegalArgumentException("Telemetry delegates must not contain nulls.");
        }

        this.delegates = List.copyOf(delegates);
    }

    @Override
    public void record(TelemetryEvent event) {
        for (TelemetrySink delegate : delegates) {
            delegate.record(event);
        }
    }
}
