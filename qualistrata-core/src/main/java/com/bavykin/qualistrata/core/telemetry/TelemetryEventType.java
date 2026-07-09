package com.bavykin.qualistrata.core.telemetry;

/**
 * Generic telemetry event categories used by lightweight framework
 * instrumentation.
 *
 * <p>The enum intentionally stays small. Runtime modules should express their
 * domain through metric names, tags, and fields rather than by expanding this
 * enum for every technology-specific operation.</p>
 */
public enum TelemetryEventType {

    /** Duration of an operation, usually represented in milliseconds. */
    DURATION,

    /** Monotonically increasing or point-in-time count. */
    COUNTER,

    /** Point-in-time numeric value. */
    GAUGE,

    /** Discrete diagnostic event. */
    EVENT,

    /** Time-series resource or process sample. */
    SAMPLE
}
