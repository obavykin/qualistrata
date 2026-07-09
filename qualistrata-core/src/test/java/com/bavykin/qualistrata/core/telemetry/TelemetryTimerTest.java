package com.bavykin.qualistrata.core.telemetry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelemetryTimerTest {

    @Test
    void shouldRecordSuccessfulDuration() {
        RecordingTelemetrySink sink = new RecordingTelemetrySink();

        String result = sink.timer("unix.command.execution.ms")
                .tag("module", "unix")
                .field("exitCode", 0)
                .measure(() -> "ok");

        assertEquals("ok", result);
        assertEquals(1, sink.events().size());

        TelemetryEvent event = sink.events().getFirst();
        assertEquals("unix.command.execution.ms", event.name());
        assertEquals(TelemetryEventType.DURATION, event.type());
        assertEquals("success", event.tags().get("status"));
        assertEquals("unix", event.tags().get("module"));
        assertEquals(0, event.fields().get("exitCode"));
        assertTrue(event.fields().containsKey("durationNanos"));
        assertTrue(event.fields().containsKey("durationMillis"));
    }

    @Test
    void shouldRecordFailedDurationAndRethrow() {
        RecordingTelemetrySink sink = new RecordingTelemetrySink();
        IllegalStateException failure = assertThrows(IllegalStateException.class, () ->
                sink.timer("db.statement.execution.ms")
                        .measure(() -> {
                            throw new IllegalStateException("boom");
                        })
        );

        assertEquals("boom", failure.getMessage());
        assertEquals(1, sink.events().size());

        TelemetryEvent event = sink.events().getFirst();
        assertEquals("failed", event.tags().get("status"));
        assertEquals(IllegalStateException.class.getName(), event.fields().get("exceptionType"));
    }
}
