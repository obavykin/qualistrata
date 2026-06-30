package com.bavykin.qualistrata.perf.reporting;

import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;
import com.bavykin.qualistrata.core.telemetry.TelemetryEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalJsonTelemetryReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteReportAsJsonArtifact() throws Exception {
        TelemetryReport report = TelemetryReport.of(
                "run-1",
                Map.of("target", "pt-a"),
                List.of(TelemetryEvent.builder("unix.command.execution.ms", TelemetryEventType.DURATION)
                        .tag("status", "success")
                        .field("durationMillis", 42.5)
                        .build())
        );

        Path output = tempDir.resolve("reports").resolve("telemetry.json");
        new LocalJsonTelemetryReportWriter().write(report, output);

        String json = Files.readString(output);
        assertTrue(json.contains("run-1"));
        assertTrue(json.contains("pt-a"));
        assertTrue(json.contains("unix.command.execution.ms"));
        assertTrue(json.contains("durationMillis"));
    }
}
