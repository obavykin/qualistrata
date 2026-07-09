package com.bavykin.qualistrata.core.config.model;

/**
 * Performance and telemetry-related target configuration.
 *
 * <p>This record represents the performance configuration section of a target
 * environment. It is intentionally minimal at the current stage and will be
 * extended when reusable metrics collection, local reporting, and optional
 * external publishing settings are introduced.</p>
 *
 * <p>The model must remain generic and must not contain product-specific
 * thresholds, scenario names, or environment assumptions.</p>
 */
public record PerfConfig() {
}