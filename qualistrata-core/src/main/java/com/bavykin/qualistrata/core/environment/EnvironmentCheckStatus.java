package com.bavykin.qualistrata.core.environment;

/**
 * Represents the execution status of an environment check.
 *
 * <p>The status describes what happened during check execution, while severity
 * describes how important the result is for the consuming project.</p>
 */
public enum EnvironmentCheckStatus {

    /**
     * The environment check completed successfully.
     */
    PASSED,

    /**
     * The environment check was executed and detected a problem.
     */
    FAILED,

    /**
     * The environment check was intentionally skipped.
     */
    SKIPPED
}