package com.bavykin.qualistrata.ui.reporting;

import com.bavykin.qualistrata.ui.artifact.UiArtifact;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Report entry for one UI test method.
 *
 * @param className test class name.
 * @param methodName test method name.
 * @param status final test status.
 * @param startedAt test start timestamp.
 * @param duration test duration.
 * @param groups framework/test groups.
 * @param mutationType mutation classification.
 * @param artifacts collected diagnostic artifacts.
 * @param steps logical step reports.
 * @param error optional error summary.
 */
public record UiTestReport(
        String className,
        String methodName,
        UiTestStatus status,
        Instant startedAt,
        Duration duration,
        List<String> groups,
        String mutationType,
        List<UiArtifact> artifacts,
        List<UiStepReport> steps,
        String error
) {
    /** Creates report with immutable empty-list defaults. */
    public UiTestReport {
        groups = groups == null ? List.of() : List.copyOf(groups);
        artifacts = artifacts == null ? List.of() : List.copyOf(artifacts);
        steps = steps == null ? List.of() : List.copyOf(steps);
    }
}
