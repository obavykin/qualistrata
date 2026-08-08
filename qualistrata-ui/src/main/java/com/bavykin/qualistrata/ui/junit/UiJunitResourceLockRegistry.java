package com.bavykin.qualistrata.ui.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** JVM-local fair read/write lock registry used by the JUnit UI extension. */
final class UiJunitResourceLockRegistry {

    private final ConcurrentMap<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

    Handle acquire(List<Request> requests) {
        List<Request> ordered = normalize(requests);
        List<Lock> acquired = new ArrayList<>(ordered.size());

        try {
            for (Request request : ordered) {
                ReentrantReadWriteLock shared = locks.computeIfAbsent(
                        request.key(),
                        ignored -> new ReentrantReadWriteLock(true)
                );
                Lock lock = request.mode() == SharedResourceAccessMode.WRITE
                        ? shared.writeLock()
                        : shared.readLock();
                lock.lockInterruptibly();
                acquired.add(lock);
            }
            return new Handle(acquired);
        } catch (InterruptedException interrupted) {
            unlockReverse(acquired);
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting for QualiStrata shared-resource lock.",
                    interrupted
            );
        } catch (RuntimeException | Error failure) {
            unlockReverse(acquired);
            throw failure;
        }
    }

    List<Request> normalize(List<Request> requests) {
        Map<String, SharedResourceAccessMode> merged = new TreeMap<>();
        for (Request request : requests == null ? List.<Request>of() : requests) {
            Objects.requireNonNull(request, "Shared-resource lock request must not be null.");
            merged.merge(request.key(), request.mode(), UiJunitResourceLockRegistry::strongest);
        }
        return merged.entrySet().stream()
                .map(entry -> new Request(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static SharedResourceAccessMode strongest(
            SharedResourceAccessMode left,
            SharedResourceAccessMode right
    ) {
        return left == SharedResourceAccessMode.WRITE || right == SharedResourceAccessMode.WRITE
                ? SharedResourceAccessMode.WRITE
                : SharedResourceAccessMode.READ;
    }

    private static void unlockReverse(List<Lock> acquired) {
        List<Lock> reverse = new ArrayList<>(acquired);
        Collections.reverse(reverse);
        reverse.forEach(Lock::unlock);
    }

    record Request(String key, SharedResourceAccessMode mode) {

        Request {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Shared-resource lock key must not be blank.");
            }
            Objects.requireNonNull(mode, "Shared-resource lock mode must not be null.");
        }
    }

    static final class Handle implements AutoCloseable {

        private final List<Lock> acquired;
        private final AtomicBoolean closed = new AtomicBoolean();

        private Handle(List<Lock> acquired) {
            this.acquired = List.copyOf(acquired);
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                unlockReverse(acquired);
            }
        }
    }
}
