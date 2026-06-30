package com.bavykin.qualistrata.core.telemetry;

import java.util.ArrayList;
import java.util.List;

final class RecordingTelemetrySink implements TelemetrySink {

    private final List<TelemetryEvent> events = new ArrayList<>();

    @Override
    public void record(TelemetryEvent event) {
        events.add(event);
    }

    List<TelemetryEvent> events() {
        return events;
    }
}
