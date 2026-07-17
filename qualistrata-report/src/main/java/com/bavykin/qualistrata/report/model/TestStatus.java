package com.bavykin.qualistrata.report.model;

/**
 * Framework-neutral final or attempt status used by QualiStrata test-run reports.
 *
 * <p>Framework adapters are responsible for translating their native execution
 * states into this common model. In particular, {@link #SKIPPED} and
 * {@link #ABORTED} remain distinct because an explicitly skipped test and an
 * execution aborted by an assumption or another runtime condition do not carry
 * the same meaning.</p>
 */
public enum TestStatus {

    /** The test or attempt completed successfully. */
    PASSED,

    /** The test or attempt completed with a failure. */
    FAILED,

    /** The test or attempt was not executed because it was skipped. */
    SKIPPED,

    /** The test or attempt started or was selected but did not complete normally. */
    ABORTED
}
