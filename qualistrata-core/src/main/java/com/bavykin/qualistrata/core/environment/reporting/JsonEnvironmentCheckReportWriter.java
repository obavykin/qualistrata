package com.bavykin.qualistrata.core.environment.reporting;

import com.bavykin.qualistrata.core.environment.EnvironmentCheckResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSuiteResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * Default JSON writer for generic environment check suite reports.
 *
 * <p>This writer produces a stable, framework-level JSON structure based on
 * {@link EnvironmentCheckSuiteResult}. It is intended for diagnostics, CI
 * artifacts, and machine-readable troubleshooting.</p>
 *
 * <p>Consuming projects may provide their own
 * {@link EnvironmentCheckReportWriter} implementation when they need a
 * project-specific JSON schema or report structure.</p>
 */
public class JsonEnvironmentCheckReportWriter implements EnvironmentCheckReportWriter {

    /**
     * Creates a default JSON environment check report writer.
     */
    public JsonEnvironmentCheckReportWriter() {
        // Default writer has no configurable state.
    }

    /**
     * Writes an environment check suite report as JSON.
     *
     * @param suite environment check suite result.
     * @param outputFile output JSON file.
     */
    @Override
    public void write(EnvironmentCheckSuiteResult suite, Path outputFile) {
        validateInput(suite, outputFile);
        createParentDirectory(outputFile);

        try {
            Files.writeString(outputFile, toJson(suite));
        } catch (IOException exception) {
            throw new EnvironmentCheckReportException(
                    "Failed to write environment check JSON report: " + outputFile,
                    exception
            );
        }
    }

    /**
     * Converts suite result to JSON.
     *
     * @param suite suite result.
     * @return JSON text.
     */
    private String toJson(EnvironmentCheckSuiteResult suite) {
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        appendStringField(json, "name", suite.name(), 1, true);
        appendNumberField(json, "durationMillis", durationMillis(suite.duration()), 1, true);
        appendNumberField(json, "passedCount", suite.passedCount(), 1, true);
        appendNumberField(json, "failedCount", suite.failedCount(), 1, true);
        appendNumberField(json, "skippedCount", suite.skippedCount(), 1, true);
        appendBooleanField(json, "allPassed", suite.allPassed(), 1, true);
        appendBooleanField(json, "hasFailures", suite.hasFailures(), 1, true);
        appendBooleanField(json, "hasBlockingFailures", suite.hasBlockingFailures(), 1, true);

        indent(json, 1).append("\"results\": [\n");

        for (int index = 0; index < suite.results().size(); index++) {
            appendResult(json, suite.results().get(index), 2);

            if (index < suite.results().size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        indent(json, 1).append("]\n");
        json.append("}\n");

        return json.toString();
    }

    /**
     * Appends one check result as JSON object.
     *
     * @param json JSON builder.
     * @param result check result.
     * @param level indentation level.
     */
    private void appendResult(
            StringBuilder json,
            EnvironmentCheckResult result,
            int level
    ) {
        indent(json, level).append("{\n");

        appendStringField(json, "id", result.id(), level + 1, true);
        appendStringField(json, "name", result.name(), level + 1, true);
        appendStringField(json, "status", result.status().name(), level + 1, true);
        appendStringField(json, "severity", result.severity().name(), level + 1, true);
        appendStringField(json, "message", result.message(), level + 1, true);
        appendNumberField(json, "durationMillis", durationMillis(result.duration()), level + 1, true);
        appendBooleanField(json, "blockingFailure", result.isBlockingFailure(), level + 1, true);

        indent(json, level + 1).append("\"details\": ");
        appendDetails(json, result.details(), level + 1);
        json.append(",\n");

        indent(json, level + 1).append("\"error\": ");
        appendError(json, result.error(), level + 1);
        json.append("\n");

        indent(json, level).append("}");
    }

    /**
     * Appends details map as JSON object.
     *
     * @param json JSON builder.
     * @param details diagnostic details.
     * @param level indentation level.
     */
    private void appendDetails(
            StringBuilder json,
            Map<String, String> details,
            int level
    ) {
        json.append("{");

        if (!details.isEmpty()) {
            json.append("\n");

            int index = 0;

            for (Map.Entry<String, String> entry : details.entrySet()) {
                appendStringField(
                        json,
                        entry.getKey(),
                        entry.getValue(),
                        level + 1,
                        index < details.size() - 1
                );

                index++;
            }

            indent(json, level);
        }

        json.append("}");
    }

    /**
     * Appends error as JSON object or null.
     *
     * @param json JSON builder.
     * @param error optional error.
     * @param level indentation level.
     */
    private void appendError(
            StringBuilder json,
            Throwable error,
            int level
    ) {
        if (error == null) {
            json.append("null");

            return;
        }

        json.append("{\n");
        appendStringField(json, "type", error.getClass().getName(), level + 1, true);
        appendStringField(json, "message", error.getMessage(), level + 1, false);
        indent(json, level).append("}");
    }

    /**
     * Appends a JSON string field.
     *
     * @param json JSON builder.
     * @param name field name.
     * @param value field value.
     * @param level indentation level.
     * @param comma whether to append comma.
     */
    private void appendStringField(
            StringBuilder json,
            String name,
            String value,
            int level,
            boolean comma
    ) {
        indent(json, level)
                .append("\"")
                .append(escapeJson(name))
                .append("\": ");

        if (value == null) {
            json.append("null");
        } else {
            json.append("\"")
                    .append(escapeJson(value))
                    .append("\"");
        }

        appendCommaAndNewLine(json, comma);
    }

    /**
     * Appends a JSON number field.
     *
     * @param json JSON builder.
     * @param name field name.
     * @param value field value.
     * @param level indentation level.
     * @param comma whether to append comma.
     */
    private void appendNumberField(
            StringBuilder json,
            String name,
            long value,
            int level,
            boolean comma
    ) {
        indent(json, level)
                .append("\"")
                .append(escapeJson(name))
                .append("\": ")
                .append(value);

        appendCommaAndNewLine(json, comma);
    }

    /**
     * Appends a JSON boolean field.
     *
     * @param json JSON builder.
     * @param name field name.
     * @param value field value.
     * @param level indentation level.
     * @param comma whether to append comma.
     */
    private void appendBooleanField(
            StringBuilder json,
            String name,
            boolean value,
            int level,
            boolean comma
    ) {
        indent(json, level)
                .append("\"")
                .append(escapeJson(name))
                .append("\": ")
                .append(value);

        appendCommaAndNewLine(json, comma);
    }

    /**
     * Appends comma and line break.
     *
     * @param json JSON builder.
     * @param comma whether to append comma.
     */
    private void appendCommaAndNewLine(StringBuilder json, boolean comma) {
        if (comma) {
            json.append(",");
        }

        json.append("\n");
    }

    /**
     * Converts duration to milliseconds.
     *
     * @param duration optional duration.
     * @return duration in milliseconds or {@code 0}.
     */
    private long durationMillis(Duration duration) {
        return duration == null ? 0 : duration.toMillis();
    }

    /**
     * Escapes a JSON string value.
     *
     * @param value value to escape.
     * @return escaped value.
     */
    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    /**
     * Appends indentation.
     *
     * @param builder text builder.
     * @param level indentation level.
     * @return same builder.
     */
    private StringBuilder indent(StringBuilder builder, int level) {
        return builder.append("    ".repeat(level));
    }

    /**
     * Validates writer input.
     *
     * @param suite environment check suite result.
     * @param outputFile output file.
     */
    private void validateInput(EnvironmentCheckSuiteResult suite, Path outputFile) {
        if (suite == null) {
            throw new EnvironmentCheckReportException("Environment check suite result must not be null.");
        }

        if (outputFile == null) {
            throw new EnvironmentCheckReportException("Environment check report output file must not be null.");
        }
    }

    /**
     * Creates parent directory for the output file if it exists.
     *
     * @param outputFile output file.
     */
    private void createParentDirectory(Path outputFile) {
        Path parent = outputFile.getParent();

        if (parent == null) {
            return;
        }

        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new EnvironmentCheckReportException(
                    "Failed to create environment check report directory: " + parent,
                    exception
            );
        }
    }
}