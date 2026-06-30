package com.bavykin.qualistrata.perf.sink;

import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;
import com.bavykin.qualistrata.core.telemetry.TelemetrySink;

import java.util.function.Consumer;

/**
 * Protective telemetry sink wrapper.
 *
 * <p>Normal functional, smoke, and regression tests must not fail only because
 * telemetry publishing failed. This wrapper catches runtime failures from the
 * wrapped sink and reports them to a diagnostic consumer. Dedicated performance
 * pipelines can use the delegate directly or add a strict variant later.</p>
 */
public class SafeTelemetrySink implements TelemetrySink {

    private final TelemetrySink delegate;
    private final Consumer<RuntimeException> failureHandler;

    /**
     * Creates a safe sink.
     *
     * @param delegate sink to protect.
     * @param failureHandler handler for swallowed publishing failures.
     */
    public SafeTelemetrySink(
            TelemetrySink delegate,
            Consumer<RuntimeException> failureHandler
    ) {
        if (delegate == null) {
            throw new IllegalArgumentException("Telemetry delegate must not be null.");
        }

        if (failureHandler == null) {
            throw new IllegalArgumentException("Telemetry failure handler must not be null.");
        }

        this.delegate = delegate;
        this.failureHandler = failureHandler;
    }

    @Override
    public void record(TelemetryEvent event) {
        try {
            delegate.record(event);
        } catch (RuntimeException exception) {
            failureHandler.accept(exception);
        }
    }
}
