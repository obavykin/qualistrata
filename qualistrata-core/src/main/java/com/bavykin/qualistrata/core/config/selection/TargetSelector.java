package com.bavykin.qualistrata.core.config.selection;

import com.bavykin.qualistrata.core.exceptions.FrameworkException;

import java.util.Map;
import java.util.Properties;

/**
 * Selects the active target name from runtime inputs.
 *
 * <p>This selector preserves the generic part of the original configuration
 * resolution idea: command-line system properties should have higher priority
 * than environment variables. Unlike the old project-specific implementation,
 * this class does not contain a hardcoded framework fallback such as
 * {@code release_6.6}, {@code dev}, or {@code local}.</p>
 *
 * <p>If a consuming project wants a default target, it must provide that default
 * explicitly when calling {@link #select(Properties, Map, String)}.</p>
 */
public class TargetSelector {

    /**
     * Creates a target selector.
     *
     * <p>The selector is stateless. A single instance can be reused by
     * project-specific configuration resolution code.</p>
     */
    public TargetSelector() {
    }

    /**
     * JVM system property used to select the active target.
     */
    public static final String TARGET_SYSTEM_PROPERTY = "target";

    /**
     * Environment variable used to select the active target.
     */
    public static final String TARGET_ENVIRONMENT_VARIABLE = "TARGET";

    /**
     * Selects the target from system properties and environment variables.
     *
     * <p>No default target is used by this method. If neither runtime source
     * provides a target name, the method fails fast with a clear framework
     * exception.</p>
     *
     * @param properties JVM system properties or a test-provided equivalent.
     * @param environment operating system environment variables or a
     *                    test-provided equivalent.
     * @return selected target with source information.
     * @throws FrameworkException if no target can be selected.
     */
    public SelectedTarget select(Properties properties, Map<String, String> environment) {
        return select(properties, environment, null);
    }

    /**
     * Selects the target from system properties, environment variables, or an
     * explicit project-level default.
     *
     * <p>Selection priority:</p>
     *
     * <ol>
     *     <li>{@code -Dtarget=...}</li>
     *     <li>{@code TARGET=...}</li>
     *     <li>explicit default provided by the consuming project</li>
     * </ol>
     *
     * <p>Blank values are ignored. This prevents accidental empty system
     * properties or environment variables from silently selecting an invalid
     * target.</p>
     *
     * @param properties JVM system properties or a test-provided equivalent.
     * @param environment operating system environment variables or a
     *                    test-provided equivalent.
     * @param projectDefault optional default target supplied by the consuming
     *                       project. QualiStrata core must not define this value.
     * @return selected target with source information.
     * @throws FrameworkException if no target can be selected.
     */
    public SelectedTarget select(
            Properties properties,
            Map<String, String> environment,
            String projectDefault
    ) {
        String systemPropertyTarget = normalize(properties == null
                ? null
                : properties.getProperty(TARGET_SYSTEM_PROPERTY));

        if (systemPropertyTarget != null) {
            return new SelectedTarget(systemPropertyTarget, TargetSelectionSource.SYSTEM_PROPERTY);
        }

        String environmentTarget = normalize(environment == null
                ? null
                : environment.get(TARGET_ENVIRONMENT_VARIABLE));

        if (environmentTarget != null) {
            return new SelectedTarget(environmentTarget, TargetSelectionSource.ENVIRONMENT_VARIABLE);
        }

        String defaultTarget = normalize(projectDefault);

        if (defaultTarget != null) {
            return new SelectedTarget(defaultTarget, TargetSelectionSource.PROJECT_DEFAULT);
        }

        throw new FrameworkException(
                "Target was not specified. Provide -Dtarget, TARGET environment variable, "
                        + "or an explicit project-level default."
        );
    }

    /**
     * Selects the target from the current JVM process runtime inputs.
     *
     * <p>This convenience method reads {@link System#getProperties()} and
     * {@link System#getenv()}. Tests should prefer the overload that accepts
     * explicit maps and properties to avoid mutating global process state.</p>
     *
     * @return selected target with source information.
     */
    public SelectedTarget selectFromCurrentProcess() {
        return select(System.getProperties(), System.getenv());
    }

    /**
     * Selects the target from the current JVM process runtime inputs and an
     * explicit project-level default.
     *
     * <p>The default is provided by the consuming project, not by QualiStrata
     * core.</p>
     *
     * @param projectDefault optional project-level default target.
     * @return selected target with source information.
     */
    public SelectedTarget selectFromCurrentProcess(String projectDefault) {
        return select(System.getProperties(), System.getenv(), projectDefault);
    }

    /**
     * Converts blank strings to {@code null} and trims non-blank values.
     *
     * @param value raw target name value.
     * @return trimmed value or {@code null} when the input is missing or blank.
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}