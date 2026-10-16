package com.bavykin.qualistrata.ui.reporting;

import com.bavykin.qualistrata.ui.UiAutomationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes a compact human-readable HTML UI report.
 */
public final class HtmlUiReportWriter implements UiReportWriter {

    /**
     * Creates an HTML UI report writer.
     */
    public HtmlUiReportWriter() {
    }

    @Override
    public void write(UiRunReport report, Path outputDirectory) {
        try {
            Files.createDirectories(outputDirectory);
            Files.writeString(outputDirectory.resolve("ui-report.html"), html(report));
        } catch (IOException exception) {
            throw new UiAutomationException("Failed to write UI HTML report.", exception);
        }
    }

    private String html(UiRunReport report) {
        StringBuilder builder = new StringBuilder();
        builder.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>QualiStrata UI Report</title>")
                .append("<style>body{font-family:Arial,sans-serif;margin:24px}table{border-collapse:collapse;width:100%}")
                .append("td,th{border:1px solid #ddd;padding:6px}th{background:#f5f5f5;text-align:left}</style></head><body>")
                .append("<h1>QualiStrata UI Report</h1>")
                .append("<p><b>Run:</b> ").append(escape(report.runId())).append("</p>")
                .append("<p><b>Engine:</b> ").append(report.engine()).append(" / ").append(report.browser()).append("</p>")
                .append("<p><b>Total:</b> ").append(report.totalTests())
                .append("; <b>Passed:</b> ").append(report.passed())
                .append("; <b>Failed:</b> ").append(report.failed())
                .append("; <b>Skipped:</b> ").append(report.skipped()).append("</p>")
                .append("<table><thead><tr><th>Class</th><th>Method</th><th>Status</th><th>Duration ms</th><th>Artifacts</th><th>Error</th></tr></thead><tbody>");

        for (UiTestReport test : report.tests()) {
            builder.append("<tr><td>").append(escape(test.className()))
                    .append("</td><td>").append(escape(test.methodName()))
                    .append("</td><td>").append(test.status())
                    .append("</td><td>").append(test.duration() == null ? "" : test.duration().toMillis())
                    .append("</td><td>").append(artifacts(test))
                    .append("</td><td>").append(escape(test.error()))
                    .append("</td></tr>");
        }

        return builder.append("</tbody></table></body></html>").toString();
    }

    private String artifacts(UiTestReport test) {
        if (test.artifacts().isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < test.artifacts().size(); i++) {
            if (i > 0) {
                builder.append("<br>");
            }
            var artifact = test.artifacts().get(i);
            builder.append(artifact.type()).append(": ").append(escape(artifact.path().toString()));
        }
        return builder.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
