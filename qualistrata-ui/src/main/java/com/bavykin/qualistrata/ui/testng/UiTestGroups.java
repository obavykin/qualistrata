package com.bavykin.qualistrata.ui.testng;

/**
 * Conventional TestNG group names for UI mutation classification.
 *
 * <p>Annotations are the preferred classification mechanism because they are
 * strongly typed and visible next to the test. Group names remain useful when a
 * consumer project already uses TestNG XML/group-based suite composition.</p>
 */
public final class UiTestGroups {

    /** Tests that only read/navigate and may normally run in parallel. */
    public static final String NON_MUTATING = "ui-non-mutating";

    /** Tests that change product data and should run sequentially by default. */
    public static final String MUTATING = "ui-mutating";

    /** Tests that require exclusive access to the target environment. */
    public static final String EXCLUSIVE = "ui-exclusive";

    private UiTestGroups() {
        // Utility class.
    }
}
