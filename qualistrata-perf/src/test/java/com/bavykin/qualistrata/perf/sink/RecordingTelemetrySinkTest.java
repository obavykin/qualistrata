package com.bavykin.qualistrata.perf.sink;

import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;
import com.bavykin.qualistrata.core.telemetry.TelemetryEventType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecordingTelemetrySinkTest {

    @Test
    void shouldStoreEventsAndReturnImmutableSnapshot() {
        RecordingTelemetrySink sink = new RecordingTelemetrySink();
        TelemetryEvent event = TelemetryEvent.builder("test.metric", TelemetryEventType.COUNTER)
                .field("value", 1)
                .build();

        sink.record(event);

        List<TelemetryEvent> events = sink.events();
        assertEquals(1, sink.size());
        assertEquals(event, events.getFirst());
        assertThrows(UnsupportedOperationException.class, () -> events.add(event));
    }

    @Test
    void shouldClearEvents() {
        RecordingTelemetrySink sink = new RecordingTelemetrySink();
        sink.record(TelemetryEvent.builder("test.metric", TelemetryEventType.EVENT).build());

        sink.clear();

        assertEquals(0, sink.size());
        assertTrue(sink.events().isEmpty());
    }
}
