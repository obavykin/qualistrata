package com.bavykin.qualistrata.perf.reporting;

import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

/**
 * Writes telemetry reports as local JSON artifacts.
 *
 * <p>Local JSON is the first reliable performance artifact. It must remain
 * available even when future external metrics publishing, such as InfluxDB, is
 * disabled or unavailable.</p>
 *
 * <p>The writer intentionally avoids adding extra JSON dependencies to
 * {@code qualistrata-perf}. The schema is small and controlled by the framework,
 * so a compact manual writer is sufficient for the first increment.</p>
 */
public class LocalJsonTelemetryReportWriter {

    /**
     * Creates a local JSON telemetry report writer.
     */
    public LocalJsonTelemetryReportWriter() {
    }

    /**
     * Writes report to the supplied path, creating parent directories when
     * needed.
     *
     * @param report telemetry report.
     * @param outputFile target JSON file.
     */
    public void write(TelemetryReport report, Path outputFile) {
        if (report == null) {
            throw new IllegalArgumentException("Telemetry report must not be null.");
        }

        if (outputFile == null) {
            throw new IllegalArgumentException("Telemetry report output file must not be null.");
        }

        try {
            if (outputFile.getParent() != null) {
                Files.createDirectories(outputFile.getParent());
            }

            Files.writeString(outputFile, toJson(report));
        } catch (IOException exception) {
            throw new TelemetryReportWriterException("Failed to write telemetry report: " + outputFile, exception);
        }
    }

    private String toJson(TelemetryReport report) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        appendStringField(json, "runId", report.runId(), 1, true);
        appendStringField(json, "createdAt", report.createdAt().toString(), 1, true);
        appendStringMap(json, "tags", report.tags(), 1, true);
        indent(json, 1).append("\"events\": [\n");

        for (int index = 0; index < report.events().size(); index++) {
            appendEvent(json, report.events().get(index), 2);

            if (index < report.events().size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        indent(json, 1).append("]\n");
        json.append("}\n");
        return json.toString();
    }

    private void appendEvent(StringBuilder json, TelemetryEvent event, int level) {
        indent(json, level).append("{\n");
        appendStringField(json, "name", event.name(), level + 1, true);
        appendStringField(json, "type", event.type().name(), level + 1, true);
        appendStringField(json, "timestamp", event.timestamp().toString(), level + 1, true);
        appendStringMap(json, "tags", event.tags(), level + 1, true);
        appendObjectMap(json, "fields", event.fields(), level + 1, false);
        json.append("\n");
        indent(json, level).append("}");
    }

    private void appendStringMap(
            StringBuilder json,
            String name,
            Map<String, String> map,
            int level,
            boolean comma
    ) {
        indent(json, level).append("\"").append(escape(name)).append("\": {");

        if (!map.isEmpty()) {
            json.append("\n");
            int index = 0;

            for (Map.Entry<String, String> entry : map.entrySet()) {
                appendStringField(json, entry.getKey(), entry.getValue(), level + 1, index < map.size() - 1);
                index++;
            }

            indent(json, level);
        }

        json.append("}");

        if (comma) {
            json.append(",");
        }

        json.append("\n");
    }

    private void appendObjectMap(
            StringBuilder json,
            String name,
            Map<String, Object> map,
            int level,
            boolean comma
    ) {
        indent(json, level).append("\"").append(escape(name)).append("\": {");

        if (!map.isEmpty()) {
            json.append("\n");
            int index = 0;

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                appendObjectField(json, entry.getKey(), entry.getValue(), level + 1, index < map.size() - 1);
                index++;
            }

            indent(json, level);
        }

        json.append("}");

        if (comma) {
            json.append(",");
        }
    }

    private void appendStringField(
            StringBuilder json,
            String name,
            String value,
            int level,
            boolean comma
    ) {
        indent(json, level)
                .append("\"").append(escape(name)).append("\": ")
                .append(value == null ? "null" : "\"" + escape(value) + "\"");

        if (comma) {
            json.append(",");
        }

        json.append("\n");
    }

    private void appendObjectField(
            StringBuilder json,
            String name,
            Object value,
            int level,
            boolean comma
    ) {
        indent(json, level)
                .append("\"").append(escape(name)).append("\": ")
                .append(toJsonValue(value));

        if (comma) {
            json.append(",");
        }

        json.append("\n");
    }

    private String toJsonValue(Object value) {
        return switch (value) {
            case null -> "null";
            case Number ignored -> value.toString();
            case Boolean ignored -> value.toString();
            case Instant instant -> "\"" + escape(instant.toString()) + "\"";
            default -> "\"" + escape(String.valueOf(value)) + "\"";
        };
    }

    private StringBuilder indent(StringBuilder json, int level) {
        return json.append("  ".repeat(level));
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
