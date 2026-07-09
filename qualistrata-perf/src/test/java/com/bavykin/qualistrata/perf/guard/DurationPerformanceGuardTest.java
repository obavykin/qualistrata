package com.bavykin.qualistrata.perf.guard;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DurationPerformanceGuardTest {

    @Test
    void shouldReturnOperationResultWhenDurationIsWithinGuard() {
        String result = new DurationPerformanceGuard("fast-operation", Duration.ofSeconds(1))
                .verify(() -> "ok");

        assertEquals("ok", result);
    }

    @Test
    void shouldFailWhenDurationExceedsGuard() {
        DurationPerformanceGuard guard = new DurationPerformanceGuard("slow-operation", Duration.ofNanos(1));

        assertThrows(PerformanceGuardException.class, () -> guard.verify(() -> {
            for (int index = 0; index < 1000; index++) {
                Math.sqrt(index);
            }
        }));
    }
}
