package com.bavykin.qualistrata.report.writer;

import com.bavykin.qualistrata.report.model.TestRun;

import java.nio.file.Path;

/**
 * Writes an immutable test-run model to a report artifact.
 */
public interface TestRunWriter {

    /**
     * Writes the supplied test run to the target path.
     *
     * @param testRun immutable test-run model
     * @param outputPath output artifact path
     * @throws ReportWriteException when serialization or file writing fails
     */
    void write(TestRun testRun, Path outputPath);
}
