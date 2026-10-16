package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinalStatusPolicyTest {

    @Test
    void resolvesStatusFromLastAttempt() {
        List<TestAttempt> attempts = List.of(
                new TestAttempt(1, TestModelFixtures.failedResult()),
                new TestAttempt(2, TestModelFixtures.passedResult(100))
        );

        assertEquals(
                TestStatus.PASSED,
                FinalStatusPolicy.LAST_ATTEMPT.resolve(attempts)
        );
    }

    @Test
    void rejectsEmptyAttemptHistory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> FinalStatusPolicy.LAST_ATTEMPT.resolve(List.of())
        );
    }
}
