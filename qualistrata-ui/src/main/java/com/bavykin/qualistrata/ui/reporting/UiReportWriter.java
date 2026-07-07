package com.bavykin.qualistrata.ui.reporting;

import java.nio.file.Path;

/**
 * Writes UI run reports to local artifacts.
 */
public interface UiReportWriter {

    /**
     * Writes report.
     *
     * @param report report to write.
     * @param outputDirectory output directory.
     */
    void write(UiRunReport report, Path outputDirectory);
}
