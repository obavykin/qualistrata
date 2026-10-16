package com.bavykin.qualistrata.perf.sink;

import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;
import com.bavykin.qualistrata.core.telemetry.TelemetrySink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory {@link TelemetrySink} implementation.
 *
 * <p>This sink is intentionally simple and belongs to {@code qualistrata-perf},
 * not {@code qualistrata-core}. Runtime modules should depend only on the core
 * {@link TelemetrySink} contract, while tests, local reports, and future
 * publisher pipelines can use this recording implementation as one concrete
 * destination.</p>
 *
 * <p>The implementation is synchronized because TestNG/JUnit scenarios may run
 * in parallel later. It favours predictable behaviour and defensive copies over
 * maximum throughput.</p>
 */
public class RecordingTelemetrySink implements TelemetrySink {

    /**
     * Creates an empty in-memory telemetry sink.
     */
    public RecordingTelemetrySink() {
    }

    private final List<TelemetryEvent> events = new ArrayList<>();

    /**
     * Records an event in insertion order.
     *
     * @param event event to record.
     */
    @Override
    public synchronized void record(TelemetryEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Telemetry event must not be null.");
        }

        events.add(event);
    }

    /**
     * Returns an immutable snapshot of recorded events.
     *
     * @return recorded event snapshot.
     */
    public synchronized List<TelemetryEvent> events() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    /**
     * Removes all previously recorded events.
     */
    public synchronized void clear() {
        events.clear();
    }

    /**
     * Returns number of recorded events.
     *
     * @return event count.
     */
    public synchronized int size() {
        return events.size();
    }
}
