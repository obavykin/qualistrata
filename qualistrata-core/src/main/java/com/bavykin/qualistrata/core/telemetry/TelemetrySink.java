package com.bavykin.qualistrata.core.telemetry;

/**
 * Minimal telemetry sink used by QualiStrata runtime modules.
 *
 * <p>The interface is intentionally lightweight and belongs to core. Full
 * performance reporting, local JSON/HTML artifacts, safe publisher composition,
 * and future InfluxDB integration belong to {@code qualistrata-perf}.</p>
 */
public interface TelemetrySink {

    /**
     * Records a telemetry event.
     *
     * @param event event to record.
     */
    void record(TelemetryEvent event);

    /**
     * Creates a timer bound to this sink.
     *
     * @param name duration metric name.
     * @return telemetry timer.
     */
    default TelemetryTimer timer(String name) {
        return new TelemetryTimer(this, name);
    }
}
