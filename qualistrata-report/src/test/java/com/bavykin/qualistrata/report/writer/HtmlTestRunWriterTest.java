package com.bavykin.qualistrata.report.writer;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void includesAutomaticAndExplicitDurationUnitControls() throws Exception {
        var templateField = HtmlTestRunWriter.class.getDeclaredField("TEMPLATE");
        templateField.setAccessible(true);
        String template = (String) templateField.get(null);

        assertTrue(template.contains("<option value=\"auto\">Auto</option>"));
        assertTrue(template.contains("<option value=\"ms\">Milliseconds</option>"));
        assertTrue(template.contains("<option value=\"sec\">Seconds</option>"));
        assertTrue(template.contains("<option value=\"min\">Minutes</option>"));
        assertTrue(template.contains("<option value=\"h\">Hours</option>"));
        assertTrue(template.contains("durationMs<1000"));
        assertTrue(template.contains("durationMs<120000"));
        assertTrue(template.contains("durationMs<3600000"));
        assertTrue(template.contains("timeUnit.onchange=renderDurations"));
    }
}
