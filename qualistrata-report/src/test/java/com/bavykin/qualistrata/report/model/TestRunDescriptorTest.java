package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestRunDescriptorTest {

    @Test
    void normalizesRunIdentity() {
        Instant startedAt = Instant.parse("2026-07-17T08:00:00Z");

        TestRunDescriptor descriptor = new TestRunDescriptor(
                " run-1 ",
                " regressionTest ",
                startedAt
        );

        assertEquals("run-1", descriptor.runId());
        assertEquals("regressionTest", descriptor.source());
        assertEquals(startedAt, descriptor.startedAt());
    }

    @Test
    void rejectsBlankRunId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TestRunDescriptor(
                        " ",
                        "test",
                        Instant.parse("2026-07-17T08:00:00Z")
                )
        );
    }
}
