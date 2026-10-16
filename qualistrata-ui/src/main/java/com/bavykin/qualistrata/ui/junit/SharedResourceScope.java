package com.bavykin.qualistrata.ui.junit;

/**
 * Defines how a shared-resource name is scoped when parallel tests are locked.
 */
public enum SharedResourceScope {

    /**
     * Resource identity includes the effective {@code target} property supplied
     * to the JUnit UI extension. Tests using different targets therefore do not
     * conflict.
     */
    TARGET,

    /**
     * Resource identity is shared by every test using the same test JVM,
     * regardless of target.
     */
    GLOBAL
}
