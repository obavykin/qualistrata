package com.bavykin.qualistrata.ui.reporting;

import java.time.Duration;

/**
 * Report entry for one logical UI step.
 *
 * @param name step name.
 * @param status step status.
 * @param duration step duration.
 * @param error optional error summary.
 */
public record UiStepReport(
        String name,
        UiTestStatus status,
        Duration duration,
        String error
) {
}
