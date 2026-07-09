package com.bavykin.qualistrata.unix.command;

import com.bavykin.qualistrata.core.contracts.unix.dto.CommandResult;
import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;
import com.bavykin.qualistrata.core.telemetry.TelemetrySink;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MeasuredUnixCommandClientTest {

    @Test
    void shouldRecordCommandDurationAndResultDiagnostics() {
        RecordingSink sink = new RecordingSink();
        UnixCommandClient delegate = request -> new CommandResult(0, "hello", "");

        CommandResult result = new MeasuredUnixCommandClient(delegate, sink)
                .execute(new UnixCommandRequest("echo hello", null));

        assertEquals(0, result.exitCode());
        assertEquals(1, sink.events().size());

        TelemetryEvent event = sink.events().getFirst();
        assertEquals(MeasuredUnixCommandClient.COMMAND_EXECUTION_METRIC, event.name());
        assertEquals("unix", event.tags().get("module"));
        assertEquals("command", event.tags().get("operation"));
        assertEquals("success", event.tags().get("status"));
        assertEquals(0, event.fields().get("exitCode"));
        assertEquals(5L, event.fields().get("stdoutBytes"));
        assertEquals(0L, event.fields().get("stderrBytes"));
        assertFalse(event.fields().containsKey("command"));
    }

    @Test
    void shouldRecordFailureAndRethrow() {
        RecordingSink sink = new RecordingSink();
        UnixCommandClient delegate = request -> {
            throw new UnixCommandException("failed");
        };

        assertThrows(UnixCommandException.class, () -> new MeasuredUnixCommandClient(delegate, sink)
                .execute(new UnixCommandRequest("bad", null)));

        TelemetryEvent event = sink.events().getFirst();
        assertEquals("failed", event.tags().get("status"));
        assertEquals(UnixCommandException.class.getName(), event.fields().get("exceptionType"));
    }

    private static final class RecordingSink implements TelemetrySink {

        private final List<TelemetryEvent> events = new ArrayList<>();

        @Override
        public void record(TelemetryEvent event) {
            events.add(event);
        }

        List<TelemetryEvent> events() {
            return events;
        }
    }
}
