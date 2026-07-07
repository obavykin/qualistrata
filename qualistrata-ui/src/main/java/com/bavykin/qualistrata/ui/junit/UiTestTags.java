package com.bavykin.qualistrata.ui.junit;

/**
 * Conventional JUnit 5 tags used by the QualiStrata UI adapter.
 *
 * <p>The constants intentionally mirror the TestNG group names used by the
 * TestNG adapter so consumer projects can keep the same suite terminology
 * regardless of the selected Java test runner.</p>
 */
public final class UiTestTags {

    /** Generic tag for UI tests. */
    public static final String UI = "ui";

    /** Read-only UI tests that can normally run in parallel. */
    public static final String NON_MUTATING = "non-mutating";

    /** UI tests that change product state and should be isolated or sequential. */
    public static final String MUTATING = "mutating";

    /** UI tests that require exclusive environment access. */
    public static final String EXCLUSIVE = "exclusive";

    private UiTestTags() {
        // Constants holder.
    }
}
