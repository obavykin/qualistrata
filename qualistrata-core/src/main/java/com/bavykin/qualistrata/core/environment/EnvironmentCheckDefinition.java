package com.bavykin.qualistrata.core.environment;

/**
 * Named environment check definition.
 *
 * <p>The definition combines check metadata with executable check logic. It is
 * used by {@link EnvironmentCheckRunner} to execute checks, measure execution
 * duration, and create fallback failed results when an unexpected exception is
 * thrown by the check.</p>
 *
 * @param id stable check identifier.
 * @param name human-readable check name.
 * @param severity default check severity.
 * @param check executable environment check.
 */
public record EnvironmentCheckDefinition(
        String id,
        String name,
        EnvironmentCheckSeverity severity,
        EnvironmentCheck check
) {

    /**
     * Creates a validated environment check definition.
     *
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param severity default check severity.
     * @param check executable environment check.
     */
    public EnvironmentCheckDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Environment check definition id must not be blank.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Environment check definition name must not be blank.");
        }

        if (severity == null) {
            throw new IllegalArgumentException("Environment check definition severity must not be null.");
        }

        if (check == null) {
            throw new IllegalArgumentException("Environment check must not be null.");
        }
    }

    /**
     * Creates an error-level environment check definition.
     *
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param check executable environment check.
     * @return environment check definition.
     */
    public static EnvironmentCheckDefinition error(
            String id,
            String name,
            EnvironmentCheck check
    ) {
        return new EnvironmentCheckDefinition(
                id,
                name,
                EnvironmentCheckSeverity.ERROR,
                check
        );
    }

    /**
     * Creates a warning-level environment check definition.
     *
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param check executable environment check.
     * @return environment check definition.
     */
    public static EnvironmentCheckDefinition warning(
            String id,
            String name,
            EnvironmentCheck check
    ) {
        return new EnvironmentCheckDefinition(
                id,
                name,
                EnvironmentCheckSeverity.WARNING,
                check
        );
    }

    /**
     * Creates an info-level environment check definition.
     *
     * @param id stable check identifier.
     * @param name human-readable check name.
     * @param check executable environment check.
     * @return environment check definition.
     */
    public static EnvironmentCheckDefinition info(
            String id,
            String name,
            EnvironmentCheck check
    ) {
        return new EnvironmentCheckDefinition(
                id,
                name,
                EnvironmentCheckSeverity.INFO,
                check
        );
    }
}