package com.bavykin.qualistrata.core.config.selection;

/**
 * Source from which the active target name was selected.
 *
 * <p>The source is stored together with the selected target name to make
 * diagnostics explicit. When a test run uses an unexpected target, reports and
 * logs should be able to show whether the value came from a system property,
 * environment variable, or an explicit project-level default.</p>
 */
public enum TargetSelectionSource {

    /**
     * Target name was resolved from a JVM system property.
     */
    SYSTEM_PROPERTY,

    /**
     * Target name was resolved from an operating system environment variable.
     */
    ENVIRONMENT_VARIABLE,

    /**
     * Target name was provided by the consuming project as an explicit default.
     *
     * <p>QualiStrata core does not define product-specific defaults. This value
     * exists only to represent defaults supplied by project-specific code.</p>
     */
    PROJECT_DEFAULT
}