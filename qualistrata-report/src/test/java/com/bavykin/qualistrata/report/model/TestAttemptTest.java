package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TestAttemptTest {

    @Test
    void rejectsNonPositiveAttemptNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TestAttempt(0, TestModelFixtures.passedResult(0))
        );
    }

    @Test
    void rejectsMissingExecutionResult() {
        assertThrows(
                NullPointerException.class,
                () -> new TestAttempt(1, null)
        );
    }
}
