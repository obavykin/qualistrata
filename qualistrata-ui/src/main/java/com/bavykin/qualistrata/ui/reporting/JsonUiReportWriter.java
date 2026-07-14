package com.bavykin.qualistrata.ui.reporting;

import com.bavykin.qualistrata.ui.UiAutomationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes UI report as machine-readable JSON.
 */
public final class JsonUiReportWriter implements UiReportWriter {

    /**
     * Creates a JSON UI report writer.
     */
    public JsonUiReportWriter() {
    }

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void write(UiRunReport report, Path outputDirectory) {
        try {
            Files.createDirectories(outputDirectory);
            mapper.writeValue(outputDirectory.resolve("ui-report.json").toFile(), report);
        } catch (IOException exception) {
            throw new UiAutomationException("Failed to write UI JSON report.", exception);
        }
    }
}
