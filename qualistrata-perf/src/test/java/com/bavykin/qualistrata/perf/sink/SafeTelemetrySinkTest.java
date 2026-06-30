package com.bavykin.qualistrata.perf.sink;

import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;
import com.bavykin.qualistrata.core.telemetry.TelemetryEventType;
import com.bavykin.qualistrata.core.telemetry.TelemetrySink;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SafeTelemetrySinkTest {

    @Test
    void shouldSwallowDelegateFailureAndReportIt() {
        List<RuntimeException> failures = new ArrayList<>();
        TelemetrySink failing = event -> {
            throw new IllegalStateException("publisher is unavailable");
        };

        new SafeTelemetrySink(failing, failures::add)
                .record(TelemetryEvent.builder("metric", TelemetryEventType.EVENT).build());

        assertEquals(1, failures.size());
        assertEquals("publisher is unavailable", failures.getFirst().getMessage());
    }
}
