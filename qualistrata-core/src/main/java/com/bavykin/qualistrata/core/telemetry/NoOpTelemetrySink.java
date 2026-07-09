package com.bavykin.qualistrata.core.telemetry;

/**
 * Telemetry sink that intentionally discards all events.
 *
 * <p>This implementation is the default safety valve for runtime modules. It
 * allows DB, Unix, UI, and API code to be instrumented without forcing every
 * consumer to enable performance reporting.</p>
 */
public final class NoOpTelemetrySink implements TelemetrySink {

    /** Shared no-op sink instance. */
    public static final NoOpTelemetrySink INSTANCE = new NoOpTelemetrySink();

    private NoOpTelemetrySink() {
    }

    /**
     * Discards the event.
     *
     * @param event ignored event.
     */
    @Override
    public void record(TelemetryEvent event) {
        // Intentionally empty.
    }
}
