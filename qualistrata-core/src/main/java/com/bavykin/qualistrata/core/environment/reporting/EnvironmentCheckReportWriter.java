package com.bavykin.qualistrata.core.environment.reporting;

import com.bavykin.qualistrata.core.environment.EnvironmentCheckSuiteResult;

import java.nio.file.Path;

/**
 * Generic contract for writing environment check suite reports.
 *
 * <p>The contract is intentionally small so consuming projects can replace
 * QualiStrata's default JSON/HTML writers with project-specific report writers
 * when they need custom schema, grouping, branding, styling, or readiness
 * policy.</p>
 */
public interface EnvironmentCheckReportWriter {

    /**
     * Writes the provided environment check suite result to the given output
     * file.
     *
     * @param suite environment check suite result.
     * @param outputFile output report file.
     */
    void write(EnvironmentCheckSuiteResult suite, Path outputFile);
}