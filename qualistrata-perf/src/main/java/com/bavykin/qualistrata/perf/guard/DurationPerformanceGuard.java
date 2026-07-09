package com.bavykin.qualistrata.perf.guard;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Broad duration guard for functional and smoke flows.
 *
 * <p>The guard measures elapsed wall-clock time and fails only after the
 * operation finishes or throws. It is intentionally not a timeout mechanism and
 * does not interrupt the running operation. Runtime modules should still use
 * their own real timeouts for SSH, DB, UI, or HTTP calls.</p>
 */
public class DurationPerformanceGuard {

    private final String operationName;
    private final Duration maxDuration;

    /**
     * Creates guard.
     *
     * @param operationName logical operation name.
     * @param maxDuration maximum accepted duration.
     */
    public DurationPerformanceGuard(String operationName, Duration maxDuration) {
        if (operationName == null || operationName.isBlank()) {
            throw new IllegalArgumentException("Guard operation name must not be blank.");
        }

        if (maxDuration == null || maxDuration.isNegative() || maxDuration.isZero()) {
            throw new IllegalArgumentException("Guard duration must be positive.");
        }

        this.operationName = operationName;
        this.maxDuration = maxDuration;
    }

    /**
     * Executes operation and verifies elapsed duration.
     *
     * @param operation operation to execute.
     * @param <T> result type.
     * @return operation result.
     */
    public <T> T verify(Supplier<T> operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Guarded operation must not be null.");
        }

        long started = System.nanoTime();
        T result = operation.get();
        verifyElapsed(started);

        return result;
    }

    /**
     * Executes runnable operation and verifies elapsed duration.
     *
     * @param operation operation to execute.
     */
    public void verify(Runnable operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Guarded operation must not be null.");
        }

        verify(() -> {
            operation.run();
            return null;
        });
    }

    private void verifyElapsed(long started) {
        Duration elapsed = Duration.ofNanos(System.nanoTime() - started);

        if (elapsed.compareTo(maxDuration) > 0) {
            throw new PerformanceGuardException(
                    "Operation '" + operationName + "' exceeded performance guard. "
                            + "Elapsed: " + elapsed + ", max: " + maxDuration + "."
            );
        }
    }
}
