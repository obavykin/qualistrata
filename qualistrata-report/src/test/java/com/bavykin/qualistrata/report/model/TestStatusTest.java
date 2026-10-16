package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class TestStatusTest {

    @Test
    void exposesCanonicalFrameworkNeutralStatuses() {
        assertIterableEquals(
                List.of("PASSED", "FAILED", "SKIPPED", "ABORTED"),
                Stream.of(TestStatus.values()).map(Enum::name).toList()
        );
    }
}
