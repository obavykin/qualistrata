package com.bavykin.qualistrata.report.junit;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

/**
 * Immutable runtime configuration for automatic JUnit JSON report generation.
 *
 * <p>The default configuration enables reporting, uses {@code test} as the
 * execution source, generates a random run ID, and writes to
 * {@code build/reports/qualistrata/test/test-run.json} relative to the current
 * working directory.</p>
 *
 * @param enabled whether automatic JUnit report generation is enabled
 * @param runId stable identifier written to the test-run report
 * @param source source task, suite, or execution name
 * @param outputPath destination of the canonical JSON report
 */
public record JUnitReportConfiguration(
        boolean enabled,
        String runId,
        String source,
        Path outputPath
) {

    /**
     * System property controlling whether automatic reporting is enabled.
     */
    public static final String ENABLED_PROPERTY =
            "qualistrata.report.enabled";

    /**
     * System property overriding the generated run identifier.
     */
    public static final String RUN_ID_PROPERTY =
            "qualistrata.report.runId";

    /**
     * System property defining the source task, suite, or execution name.
     */
    public static final String SOURCE_PROPERTY =
            "qualistrata.report.source";

    /**
     * System property overriding the JSON output path.
     */
    public static final String OUTPUT_PROPERTY =
            "qualistrata.report.output";

    /**
     * Creates and validates a JUnit report configuration.
     */
    public JUnitReportConfiguration {
        runId = requireText(runId, "runId");
        source = requireText(source, "source");
        outputPath = Objects.requireNonNull(outputPath, "outputPath")
                .normalize();
    }

    /**
     * Reads configuration from the current JVM system properties.
     *
     * @return validated runtime configuration
     */
    public static JUnitReportConfiguration fromSystemProperties() {
        return fromProperties(System.getProperties(), Path.of(""));
    }

    /**
     * Reads configuration from an explicit property set.
     *
     * <p>This overload supports deterministic unit testing and embedded
     * launchers. Relative output paths are resolved against the supplied
     * working directory.</p>
     *
     * @param properties configuration properties
     * @param workingDirectory base directory for relative report paths
     * @return validated runtime configuration
     */
    public static JUnitReportConfiguration fromProperties(
            Properties properties,
            Path workingDirectory
    ) {
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(workingDirectory, "workingDirectory");

        boolean enabled = Boolean.parseBoolean(
                properties.getProperty(ENABLED_PROPERTY, "true")
        );
        String source = valueOrDefault(
                properties.getProperty(SOURCE_PROPERTY),
                "test"
        );
        String runId = valueOrDefault(
                properties.getProperty(RUN_ID_PROPERTY),
                UUID.randomUUID().toString()
        );

        String configuredOutput = properties.getProperty(OUTPUT_PROPERTY);
        Path outputPath;
        if (configuredOutput == null || configuredOutput.isBlank()) {
            outputPath = workingDirectory.resolve(
                    Path.of(
                            "build",
                            "reports",
                            "qualistrata",
                            pathSegment(source),
                            "test-run.json"
                    )
            );
        } else {
            Path configuredPath = Path.of(configuredOutput.trim());
            outputPath = configuredPath.isAbsolute()
                    ? configuredPath
                    : workingDirectory.resolve(configuredPath);
        }

        return new JUnitReportConfiguration(
                enabled,
                runId,
                source,
                outputPath
        );
    }

    private static String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static String pathSegment(String source) {
        String normalized = source.trim().replaceAll(
                "[^A-Za-z0-9._-]+",
                "_"
        );
        return normalized.isEmpty() ? "test" : normalized;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }
        return value.trim();
    }
}
