package com.bavykin.qualistrata.ui.junit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiJunitResourceLockRegistryTest {

    private static final Duration SHORT_WAIT = Duration.ofMillis(150);
    private static final Duration COMPLETION_WAIT = Duration.ofSeconds(2);

    private final UiJunitResourceLockRegistry registry = new UiJunitResourceLockRegistry();

    @Test
    void readersCanShareTheSameResource() throws Exception {
        try (UiJunitResourceLockRegistry.Handle ignored = registry.acquire(List.of(read("resource-a")))) {
            assertCanAcquireConcurrently(read("resource-a"));
        }
    }

    @Test
    void writerBlocksReaderUntilReleased() throws Exception {
        UiJunitResourceLockRegistry.Handle writer = registry.acquire(List.of(write("resource-a")));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch attempted = new CountDownLatch(1);
        CountDownLatch acquired = new CountDownLatch(1);

        try {
            Future<?> waiting = executor.submit(() -> {
                attempted.countDown();
                try (UiJunitResourceLockRegistry.Handle ignored =
                             registry.acquire(List.of(read("resource-a")))) {
                    acquired.countDown();
                }
            });

            assertTrue(attempted.await(COMPLETION_WAIT.toMillis(), TimeUnit.MILLISECONDS));
            assertFalse(acquired.await(SHORT_WAIT.toMillis(), TimeUnit.MILLISECONDS));

            writer.close();

            assertTrue(acquired.await(COMPLETION_WAIT.toMillis(), TimeUnit.MILLISECONDS));
            waiting.get(COMPLETION_WAIT.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            writer.close();
            executor.shutdownNow();
        }
    }

    @Test
    void writersForDifferentResourcesCanRunConcurrently() throws Exception {
        try (UiJunitResourceLockRegistry.Handle ignored = registry.acquire(List.of(write("resource-a")))) {
            assertCanAcquireConcurrently(write("resource-b"));
        }
    }

    @Test
    void normalizesRequestsInStableOrderAndPromotesWriteAccess() {
        List<UiJunitResourceLockRegistry.Request> normalized = registry.normalize(List.of(
                read("resource-b"),
                read("resource-a"),
                write("resource-a")
        ));

        assertEquals(
                List.of(write("resource-a"), read("resource-b")),
                normalized
        );
    }

    private void assertCanAcquireConcurrently(UiJunitResourceLockRegistry.Request request) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch acquired = new CountDownLatch(1);
        try {
            Future<?> future = executor.submit(() -> {
                try (UiJunitResourceLockRegistry.Handle ignored = registry.acquire(List.of(request))) {
                    acquired.countDown();
                }
            });
            assertTrue(acquired.await(COMPLETION_WAIT.toMillis(), TimeUnit.MILLISECONDS));
            future.get(COMPLETION_WAIT.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    private UiJunitResourceLockRegistry.Request read(String key) {
        return new UiJunitResourceLockRegistry.Request(key, SharedResourceAccessMode.READ);
    }

    private UiJunitResourceLockRegistry.Request write(String key) {
        return new UiJunitResourceLockRegistry.Request(key, SharedResourceAccessMode.WRITE);
    }
}
