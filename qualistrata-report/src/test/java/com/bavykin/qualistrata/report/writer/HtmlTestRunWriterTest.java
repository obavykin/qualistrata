package com.bavykin.qualistrata.report.writer;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HtmlTestRunWriterTest {

    @Test
    void derivesHtmlSiblingPathFromJsonOutput() {
        assertEquals(
                Path.of("build/reports/test-run.html"),
                HtmlTestRunWriter.siblingPath(
                        Path.of("build/reports/test-run.json")
                )
        );
    }

    @Test
    void appendsHtmlExtensionWhenOutputIsNotJson() {
        assertEquals(
                Path.of("build/reports/report.data.html"),
                HtmlTestRunWriter.siblingPath(
                        Path.of("build/reports/report.data")
                )
        );
    }

    @Test
    void rejectsPathWithoutFileName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> HtmlTestRunWriter.siblingPath(Path.of(""))
        );
    }
}
