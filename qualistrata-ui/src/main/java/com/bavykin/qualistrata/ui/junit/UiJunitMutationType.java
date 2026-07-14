package com.bavykin.qualistrata.ui.junit;

/**
 * Mutation classification used by the JUnit 5 UI adapter.
 *
 * <p>The order is intentionally conservative: exclusive tests are more
 * restrictive than mutating tests, and mutating tests are more restrictive than
 * non-mutating tests.</p>
 */
public enum UiJunitMutationType {
    /** Non Mutating. */
    NON_MUTATING,
    /** Mutating. */
    MUTATING,
    /** Exclusive. */
    EXCLUSIVE
}
