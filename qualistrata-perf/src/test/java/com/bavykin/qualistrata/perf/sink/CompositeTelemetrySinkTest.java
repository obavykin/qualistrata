package com.bavykin.qualistrata.perf.sink;

import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;
import com.bavykin.qualistrata.core.telemetry.TelemetryEventType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositeTelemetrySinkTest {

    @Test
    void shouldFanOutEventsToAllDelegates() {
        RecordingTelemetrySink first = new RecordingTelemetrySink();
        RecordingTelemetrySink second = new RecordingTelemetrySink();
        TelemetryEvent event = TelemetryEvent.builder("metric", TelemetryEventType.EVENT).build();

        new CompositeTelemetrySink(List.of(first, second)).record(event);

        assertEquals(event, first.events().getFirst());
        assertEquals(event, second.events().getFirst());
    }
}
