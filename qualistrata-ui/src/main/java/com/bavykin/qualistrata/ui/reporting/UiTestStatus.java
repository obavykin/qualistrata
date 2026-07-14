package com.bavykin.qualistrata.ui.reporting;

/**
 * UI test execution status used in local JSON/HTML reports.
 */
public enum UiTestStatus {
    /** Test completed successfully. */
    PASSED,
    /** Test completed with a failure. */
    FAILED,
    /** Test was skipped. */
    SKIPPED
}
