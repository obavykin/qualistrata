package com.bavykin.qualistrata.ui.wait;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for generic condition-based wait helper.
 */
class UiWaitTest {

    @Test
    void shouldReturnSuccessWhenConditionBecomesTrue() {
        AtomicInteger attempts = new AtomicInteger();

        WaitResult result = UiWait.until(
                () -> attempts.incrementAndGet() >= 2,
                Duration.ofMillis(100),
                Duration.ofMillis(1)
        );

        assertTrue(result.successful());
        assertTrue(result.attempts() >= 2);
    }

    @Test
    void shouldReturnFailureWhenConditionDoesNotBecomeTrue() {
        WaitResult result = UiWait.until(
                () -> false,
                Duration.ofMillis(5),
                Duration.ofMillis(1)
        );

        assertFalse(result.successful());
    }
}
