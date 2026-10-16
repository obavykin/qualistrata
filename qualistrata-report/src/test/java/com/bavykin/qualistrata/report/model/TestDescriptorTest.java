package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestDescriptorTest {

    @Test
    void defensivelyCopiesParametersAndTags() {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("month", "200107");
        Set<String> tags = new LinkedHashSet<>();
        tags.add("regression");

        TestDescriptor descriptor = new TestDescriptor(
                identifier(),
                "Monthly report",
                parameters,
                tags
        );

        parameters.put("state", "CA");
        tags.add("ui");

        assertEquals(Map.of("month", "200107"), descriptor.parameters());
        assertEquals(Set.of("regression"), descriptor.tags());
        assertThrows(
                UnsupportedOperationException.class,
                () -> descriptor.parameters().put("state", "CA")
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> descriptor.tags().add("ui")
        );
    }

    @Test
    void usesEmptyImmutableCollectionsWhenValuesAreAbsent() {
        TestDescriptor descriptor = new TestDescriptor(
                identifier(),
                "Monthly report",
                null,
                null
        );

        assertEquals(Map.of(), descriptor.parameters());
        assertEquals(Set.of(), descriptor.tags());
    }

    private static TestIdentifier identifier() {
        return new TestIdentifier(
                "test-id",
                TestFramework.JUNIT,
                "junit-jupiter",
                "regressionTest",
                "example.ReportTest",
                "monthlyReport",
                null
        );
    }
}
