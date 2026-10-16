package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestMetadataTest {

    @Test
    void normalizesValuesAndCopiesCoverageReferences() {
        List<TestCaseReference> references = new ArrayList<>();
        references.add(new TestCaseReference(" TC-1 ", " qTest "));

        TestMetadata metadata = new TestMetadata(
                " description ",
                " feature ",
                " component ",
                references
        );

        references.clear();

        assertEquals("description", metadata.description());
        assertEquals("feature", metadata.feature());
        assertEquals("component", metadata.component());
        assertEquals(1, metadata.coveredTestCases().size());
        assertEquals("TC-1", metadata.coveredTestCases().getFirst().id());
        assertThrows(
                UnsupportedOperationException.class,
                metadata.coveredTestCases()::clear
        );
    }
}
