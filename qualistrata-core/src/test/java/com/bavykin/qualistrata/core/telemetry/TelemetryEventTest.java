package com.bavykin.qualistrata.core.telemetry;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TelemetryEventTest {

    @Test
    void shouldCreateImmutableTelemetryEvent() {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("module", "unix");

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("durationNanos", 100L);

        TelemetryEvent event = new TelemetryEvent(
                "unix.command.execution.ms",
                TelemetryEventType.DURATION,
                Instant.EPOCH,
                tags,
                fields
        );

        tags.put("module", "changed");
        fields.put("durationNanos", 200L);

        assertEquals("unix", event.tags().get("module"));
        assertEquals(100L, event.fields().get("durationNanos"));
        assertThrows(UnsupportedOperationException.class, () -> event.tags().put("x", "y"));
    }

    @Test
    void shouldBuildTelemetryEvent() {
        TelemetryEvent event = TelemetryEvent.builder("db.connection.open.ms", TelemetryEventType.DURATION)
                .timestamp(Instant.EPOCH)
                .tag("provider", "oracle")
                .field("durationMillis", 12.5)
                .build();

        assertEquals("db.connection.open.ms", event.name());
        assertEquals(TelemetryEventType.DURATION, event.type());
        assertEquals(Instant.EPOCH, event.timestamp());
        assertEquals("oracle", event.tags().get("provider"));
        assertEquals(12.5, event.fields().get("durationMillis"));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new TelemetryEvent(
                " ",
                TelemetryEventType.EVENT,
                Instant.EPOCH,
                Map.of(),
                Map.of()
        ));
    }
}
