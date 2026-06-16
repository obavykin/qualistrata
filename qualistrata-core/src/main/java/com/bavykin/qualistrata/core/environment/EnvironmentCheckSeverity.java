package com.bavykin.qualistrata.core.environment;

/**
 * Represents how important an environment check result is.
 *
 * <p>Severity allows consuming projects to decide whether a failed or skipped
 * check should block test execution or only be reported as diagnostic
 * information.</p>
 */
public enum EnvironmentCheckSeverity {

    /**
     * Informational check result.
     */
    INFO,

    /**
     * Warning-level check result.
     *
     * <p>A warning usually means that the environment is not ideal, but test
     * execution may still continue depending on project policy.</p>
     */
    WARNING,

    /**
     * Error-level check result.
     *
     * <p>An error usually means that the environment is not ready for the
     * requested test execution.</p>
     */
    ERROR
}