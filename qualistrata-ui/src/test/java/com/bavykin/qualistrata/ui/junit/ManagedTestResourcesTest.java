package com.bavykin.qualistrata.ui.junit;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit coverage for per-test managed resource cleanup.
 */
class ManagedTestResourcesTest {

    /**
     * Verifies fluent registration and reverse-order cleanup.
     */
    @Test
    void shouldReturnRegisteredResourceAndCloseInReverseOrder() {
        ManagedTestResources resources = new ManagedTestResources();
        List<String> closeOrder = new ArrayList<>();

        RecordingResource first = new RecordingResource("first", closeOrder);
        RecordingResource second = new RecordingResource("second", closeOrder);

        assertSame(first, resources.register(first));
        assertSame(second, resources.register(second));

        resources.close();

        assertEquals(List.of("second", "first"), closeOrder);
    }

    /**
     * Verifies that one failing resource does not prevent remaining resources
     * from being closed and that later failures are preserved as suppressed
     * exceptions.
     */
    @Test
    @SuppressWarnings("resource")
    void shouldContinueCleanupAndAggregateFailures() {
        ManagedTestResources resources = new ManagedTestResources();
        List<String> closeOrder = new ArrayList<>();

        resources.register(new FailingResource("first", closeOrder));
        resources.register(new RecordingResource("middle", closeOrder));
        resources.register(new FailingResource("last", closeOrder));

        RuntimeException failure = assertThrows(RuntimeException.class, resources::close);

        assertEquals(List.of("last", "middle", "first"), closeOrder);
        assertEquals("Failed to close last", failure.getMessage());
        assertEquals(1, failure.getSuppressed().length);
        assertEquals("Failed to close first", failure.getSuppressed()[0].getMessage());
    }

    /**
     * Verifies idempotent cleanup and rejects registrations after closure.
     */
    @Test
    void shouldBeIdempotentAndRejectRegistrationAfterClose() {
        ManagedTestResources resources = new ManagedTestResources();
        List<String> closeOrder = new ArrayList<>();

        resources.register(new RecordingResource("only", closeOrder));
        resources.close();
        resources.close();

        assertEquals(List.of("only"), closeOrder);

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> resources.register(() -> { })
        );
        assertTrue(failure.getMessage().contains("already closed"));
    }

    /**
     * Simple closeable that records cleanup order.
     *
     * @param name resource identifier.
     * @param closeOrder shared cleanup log.
     */
    private record RecordingResource(String name, List<String> closeOrder)
            implements AutoCloseable {

        /**
         * Records this resource as closed.
         */
        @Override
        public void close() {
            closeOrder.add(name);
        }
    }

    /**
     * Closeable that records cleanup order and then fails.
     *
     * @param name resource identifier.
     * @param closeOrder shared cleanup log.
     */
    private record FailingResource(String name, List<String> closeOrder)
            implements AutoCloseable {

        /**
         * Records this resource as closed and raises a deterministic failure.
         */
        @Override
        public void close() {
            closeOrder.add(name);
            throw new IllegalStateException("Failed to close " + name);
        }
    }
}
