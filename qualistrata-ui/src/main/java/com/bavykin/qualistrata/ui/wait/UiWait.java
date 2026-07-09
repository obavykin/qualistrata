package com.bavykin.qualistrata.ui.wait;

import java.time.Duration;

/**
 * Small polling helper for cases where engine-native auto-waits are not enough.
 *
 * <p>Hard sleeps must not be used in tests. This helper polls a condition with
 * explicit timeout and interval and returns structured wait diagnostics.</p>
 */
public final class UiWait {

    private UiWait() {
    }

    /**
     * Waits until condition is true or timeout expires.
     *
     * @param condition condition to evaluate.
     * @param timeout maximum wait time.
     * @param interval polling interval.
     * @return wait result.
     */
    public static WaitResult until(WaitCondition condition, Duration timeout, Duration interval) {
        if (condition == null) {
            throw new IllegalArgumentException("Wait condition must not be null.");
        }
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("Wait timeout must be positive.");
        }
        if (interval == null || interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("Wait interval must be positive.");
        }

        long started = System.nanoTime();
        long deadline = started + timeout.toNanos();
        int attempts = 0;

        while (System.nanoTime() <= deadline) {
            attempts++;
            if (condition.isSatisfied()) {
                return new WaitResult(true, Duration.ofNanos(System.nanoTime() - started), attempts);
            }

            sleep(interval);
        }

        return new WaitResult(false, Duration.ofNanos(System.nanoTime() - started), attempts);
    }

    private static void sleep(Duration interval) {
        try {
            Thread.sleep(interval.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("UI wait was interrupted.", exception);
        }
    }
}
