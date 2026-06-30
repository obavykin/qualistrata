package com.bavykin.qualistrata.perf.guard;

/**
 * Assertion-style exception thrown when a broad performance guard is breached.
 *
 * <p>Guards are not precise performance regression assertions. They are safety
 * boundaries used by functional/smoke/regression tests to fail fast when an
 * operation becomes clearly unusable or hangs beyond an accepted maximum.</p>
 */
public class PerformanceGuardException extends RuntimeException {

    /**
     * Creates a guard breach exception.
     *
     * @param message breach description.
     */
    public PerformanceGuardException(String message) {
        super(message);
    }
}
