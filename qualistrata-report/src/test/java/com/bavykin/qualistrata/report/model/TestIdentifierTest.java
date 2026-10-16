package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestIdentifierTest {

    @Test
    void normalizesRequiredAndOptionalIdentityValues() {
        TestIdentifier identifier = new TestIdentifier(
                "  junit:class:test  ",
                TestFramework.JUNIT,
                " junit-jupiter ",
                " regressionTest ",
                " example.ReportTest ",
                " executes ",
                " invocation-1 "
        );

        assertEquals("junit:class:test", identifier.uniqueId());
        assertEquals("junit-jupiter", identifier.engineId());
        assertEquals("regressionTest", identifier.suiteName());
        assertEquals("example.ReportTest", identifier.className());
        assertEquals("executes", identifier.methodName());
        assertEquals("invocation-1", identifier.invocationId());
    }

    @Test
    void convertsBlankOptionalValuesToNull() {
        TestIdentifier identifier = new TestIdentifier(
                "test-id",
                TestFramework.TESTNG,
                " ",
                null,
                null,
                null,
                ""
        );

        assertNull(identifier.engineId());
        assertNull(identifier.suiteName());
        assertNull(identifier.className());
        assertNull(identifier.methodName());
        assertNull(identifier.invocationId());
    }

    @Test
    void rejectsBlankUniqueId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TestIdentifier(
                        " ",
                        TestFramework.JUNIT,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );
    }
}
