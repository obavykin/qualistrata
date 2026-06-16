/**
 * Generic environment check reporting contracts and default report writers.
 *
 * <p>This package provides a small writer contract and default JSON/HTML
 * implementations for {@link com.bavykin.qualistrata.core.environment.EnvironmentCheckSuiteResult}.</p>
 *
 * <p>The default writers are intended for generic diagnostics and CI artifacts.
 * Consuming projects may provide custom writers when they need project-specific
 * report schema, grouping, branding, styling, or readiness policy.</p>
 */
package com.bavykin.qualistrata.core.environment.reporting;