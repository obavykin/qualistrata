package com.bavykin.qualistrata.ui.junit;

/**
 * Access mode requested for a shared resource used by parallel JUnit UI tests.
 */
public enum SharedResourceAccessMode {

    /** Multiple tests may hold the resource concurrently. */
    READ,

    /** The test requires exclusive access to the resource. */
    WRITE
}
