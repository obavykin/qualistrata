package com.bavykin.qualistrata.core.environment.reporting;

import com.bavykin.qualistrata.core.environment.EnvironmentCheckResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSuiteResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * Default standalone HTML writer for generic environment check suite reports.
 *
 * <p>The generated report uses embedded CSS and does not require external
 * assets. It is intended for CI artifacts and human-readable diagnostics.</p>
 *
 * <p>Consuming projects may provide custom writers when they need
 * project-specific grouping, branding, sections, or visual style.</p>
 */
public class HtmlEnvironmentCheckReportWriter implements EnvironmentCheckReportWriter {

    /**
     * Creates a default HTML environment check report writer.
     */
    public HtmlEnvironmentCheckReportWriter() {
        // Default writer has no configurable state.
    }

    /**
     * Writes an environment check suite report as standalone HTML.
     *
     * @param suite environment check suite result.
     * @param outputFile output HTML file.
     */
    @Override
    public void write(EnvironmentCheckSuiteResult suite, Path outputFile) {
        validateInput(suite, outputFile);
        createParentDirectory(outputFile);

        try {
            Files.writeString(outputFile, toHtml(suite));
        } catch (IOException exception) {
            throw new EnvironmentCheckReportException(
                    "Failed to write environment check HTML report: " + outputFile,
                    exception
            );
        }
    }

    /**
     * Converts suite result to HTML.
     *
     * @param suite suite result.
     * @return HTML text.
     */
    private String toHtml(EnvironmentCheckSuiteResult suite) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>Environment Check Report</title>\n");
        html.append(style());
        html.append("</head>\n");
        html.append("<body>\n");

        html.append("    <h1>Environment Check Report</h1>\n");
        appendSummary(html, suite);
        appendResultsTable(html, suite);

        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    /**
     * Appends suite summary.
     *
     * @param html HTML builder.
     * @param suite suite result.
     */
    private void appendSummary(
            StringBuilder html,
            EnvironmentCheckSuiteResult suite
    ) {
        html.append("    <section class=\"summary\">\n");
        html.append("        <h2>Summary</h2>\n");
        html.append("        <table>\n");
        appendSummaryRow(html, "Suite", suite.name());
        appendSummaryRow(html, "Duration", durationText(suite.duration()));
        appendSummaryRow(html, "Passed", String.valueOf(suite.passedCount()));
        appendSummaryRow(html, "Failed", String.valueOf(suite.failedCount()));
        appendSummaryRow(html, "Skipped", String.valueOf(suite.skippedCount()));
        appendSummaryRow(html, "All Passed", String.valueOf(suite.allPassed()));
        appendSummaryRow(html, "Has Failures", String.valueOf(suite.hasFailures()));
        appendSummaryRow(html, "Has Blocking Failures", String.valueOf(suite.hasBlockingFailures()));
        html.append("        </table>\n");
        html.append("    </section>\n");
    }

    /**
     * Appends one summary row.
     *
     * @param html HTML builder.
     * @param name row name.
     * @param value row value.
     */
    private void appendSummaryRow(
            StringBuilder html,
            String name,
            String value
    ) {
        html.append("            <tr>")
                .append("<th>")
                .append(escapeHtml(name))
                .append("</th>")
                .append("<td>")
                .append(escapeHtml(value))
                .append("</td>")
                .append("</tr>\n");
    }

    /**
     * Appends individual results table.
     *
     * @param html HTML builder.
     * @param suite suite result.
     */
    private void appendResultsTable(
            StringBuilder html,
            EnvironmentCheckSuiteResult suite
    ) {
        html.append("    <section>\n");
        html.append("        <h2>Checks</h2>\n");
        html.append("        <table>\n");
        html.append("            <thead>\n");
        html.append("                <tr>\n");
        html.append("                    <th>ID</th>\n");
        html.append("                    <th>Name</th>\n");
        html.append("                    <th>Status</th>\n");
        html.append("                    <th>Severity</th>\n");
        html.append("                    <th>Duration</th>\n");
        html.append("                    <th>Message</th>\n");
        html.append("                    <th>Details</th>\n");
        html.append("                    <th>Error</th>\n");
        html.append("                </tr>\n");
        html.append("            </thead>\n");
        html.append("            <tbody>\n");

        for (EnvironmentCheckResult result : suite.results()) {
            appendResultRow(html, result);
        }

        html.append("            </tbody>\n");
        html.append("        </table>\n");
        html.append("    </section>\n");
    }

    /**
     * Appends one check result row.
     *
     * @param html HTML builder.
     * @param result check result.
     */
    private void appendResultRow(
            StringBuilder html,
            EnvironmentCheckResult result
    ) {
        String statusClass = "status-" + result.status().name().toLowerCase();

        html.append("                <tr>\n");
        appendCell(html, result.id());
        appendCell(html, result.name());
        appendCellWithClass(html, result.status().name(), statusClass);
        appendCell(html, result.severity().name());
        appendCell(html, durationText(result.duration()));
        appendCell(html, result.message());
        appendCell(html, detailsText(result.details()));
        appendCell(html, errorText(result.error()));
        html.append("                </tr>\n");
    }

    /**
     * Appends a table cell.
     *
     * @param html HTML builder.
     * @param value cell value.
     */
    private void appendCell(
            StringBuilder html,
            String value
    ) {
        html.append("                    <td>")
                .append(escapeHtml(value))
                .append("</td>\n");
    }

    /**
     * Appends a table cell with CSS class.
     *
     * @param html HTML builder.
     * @param value cell value.
     * @param cssClass CSS class.
     */
    private void appendCellWithClass(
            StringBuilder html,
            String value,
            String cssClass
    ) {
        html.append("                    <td class=\"")
                .append(escapeHtml(cssClass))
                .append("\">")
                .append(escapeHtml(value))
                .append("</td>\n");
    }

    /**
     * Converts details map to readable text.
     *
     * @param details diagnostic details.
     * @return readable details text.
     */
    private String detailsText(Map<String, String> details) {
        if (details == null || details.isEmpty()) {
            return "";
        }

        StringBuilder text = new StringBuilder();

        for (Map.Entry<String, String> entry : details.entrySet()) {
            if (!text.isEmpty()) {
                text.append("\n");
            }

            text.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }

        return text.toString();
    }

    /**
     * Converts error to readable text.
     *
     * @param error optional error.
     * @return readable error text.
     */
    private String errorText(Throwable error) {
        if (error == null) {
            return "";
        }

        return error.getClass().getName() + ": " + nullToEmpty(error.getMessage());
    }

    /**
     * Converts duration to readable text.
     *
     * @param duration optional duration.
     * @return readable duration text.
     */
    private String durationText(Duration duration) {
        if (duration == null) {
            return "";
        }

        return duration.toMillis() + " ms";
    }

    /**
     * Converts {@code null} to an empty string.
     *
     * @param value value.
     * @return original value or empty string.
     */
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * Escapes HTML text.
     *
     * @param value value to escape.
     * @return escaped text.
     */
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /**
     * Returns embedded CSS.
     *
     * @return CSS text wrapped in style tag.
     */
    private String style() {
        return """
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 24px;
                        color: #222;
                    }
                    h1, h2 {
                        color: #333;
                    }
                    table {
                        border-collapse: collapse;
                        width: 100%;
                        margin-bottom: 24px;
                    }
                    th, td {
                        border: 1px solid #ddd;
                        padding: 8px;
                        vertical-align: top;
                        white-space: pre-wrap;
                    }
                    th {
                        background-color: #f4f4f4;
                        text-align: left;
                    }
                    .summary table {
                        max-width: 640px;
                    }
                    .status-passed {
                        color: #0a7a0a;
                        font-weight: bold;
                    }
                    .status-failed {
                        color: #b00020;
                        font-weight: bold;
                    }
                    .status-skipped {
                        color: #8a6d00;
                        font-weight: bold;
                    }
                </style>
                """;
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