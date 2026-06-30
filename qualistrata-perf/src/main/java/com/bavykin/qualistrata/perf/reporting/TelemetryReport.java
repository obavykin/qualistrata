package com.bavykin.qualistrata.perf.reporting;

import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Local telemetry report model.
 *
 * <p>The report captures one logical run and its recorded telemetry events. It
 * is intentionally generic: consumer projects can use tags such as project,
 * target, release, suite, or build number to describe the run without forcing
 * TNT-specific or CI-specific concepts into QualiStrata.</p>
 *
 * @param runId logical run identifier.
 * @param createdAt report creation time.
 * @param tags low-cardinality run tags.
 * @param events recorded telemetry events.
 */
public record TelemetryReport(
        String runId,
        Instant createdAt,
        Map<String, String> tags,
        List<TelemetryEvent> events
) {

    /**
     * Creates a validated immutable report.
     */
    public TelemetryReport {
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("Telemetry report run id must not be blank.");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Telemetry report creation time must not be null.");
        }

        tags = tags == null ? Map.of() : Map.copyOf(tags);
        events = events == null ? List.of() : List.copyOf(events);
    }

    /**
     * Creates a report with the current timestamp.
     *
     * @param runId logical run identifier.
     * @param tags report tags.
     * @param events telemetry events.
     * @return report instance.
     */
    public static TelemetryReport of(
            String runId,
            Map<String, String> tags,
            List<TelemetryEvent> events
    ) {
        return new TelemetryReport(runId, Instant.now(), tags, events);
    }
}
