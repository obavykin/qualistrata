package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionProblemTest {

    @Test
    void normalizesProblemPhase() {
        ExecutionProblem problem = new ExecutionProblem(
                " beforeClass ",
                Instant.parse("2026-07-17T08:00:00Z"),
                new FailureDetails(null, "setup failed", null)
        );

        assertEquals("beforeClass", problem.phase());
    }

    @Test
    void rejectsBlankPhase() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExecutionProblem(
                        " ",
                        Instant.parse("2026-07-17T08:00:00Z"),
                        new FailureDetails(null, "setup failed", null)
                )
        );
    }
}
