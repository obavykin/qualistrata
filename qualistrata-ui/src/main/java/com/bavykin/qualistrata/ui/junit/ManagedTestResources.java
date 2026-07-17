package com.bavykin.qualistrata.ui.junit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Per-test registry for resources that must be closed before the browser test
 * session is released.
 *
 * <p>This class is package-private because consumer tests should interact with
 * it only through {@link AbstractQualiStrataJunitUiTest#manage(AutoCloseable)}.
 * Keeping the registry internal prevents consumer projects from coupling to
 * lifecycle implementation details.</p>
 */
final class ManagedTestResources implements AutoCloseable {

    private final Deque<AutoCloseable> resources = new ArrayDeque<>();
    private boolean closed;

    /**
     * Registers a resource and returns the same instance.
     *
     * <p>Resources are pushed onto a stack so cleanup occurs in reverse
     * registration order. Registration after cleanup has started is rejected,
     * because such a resource could no longer be guaranteed automatic cleanup.</p>
     *
     * @param resource non-null resource owned by the current test.
     * @param <T> concrete resource type.
     * @return the same resource instance.
     * @throws NullPointerException when {@code resource} is {@code null}.
     * @throws IllegalStateException when this registry has already been closed.
     */
    <T extends AutoCloseable> T register(T resource) {
        Objects.requireNonNull(resource, "Managed test resource must not be null.");
        if (closed) {
            throw new IllegalStateException("Managed test resource registry is already closed.");
        }
        resources.push(resource);
        return resource;
    }

    /**
     * Closes every registered resource in reverse registration order.
     *
     * <p>Cleanup is best-effort: every resource receives a close attempt even
     * when an earlier resource fails. The first failure is rethrown as a
     * {@link RuntimeException}; later failures are attached to it as suppressed
     * exceptions. Calling this method more than once is safe and has no effect
     * after the first call.</p>
     *
     * @throws RuntimeException when one or more resources fail to close.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        RuntimeException firstFailure = null;
        while (!resources.isEmpty()) {
            AutoCloseable resource = resources.pop();
            try {
                resource.close();
            } catch (Exception failure) {
                RuntimeException normalized = normalize(failure);
                if (firstFailure == null) {
                    firstFailure = normalized;
                } else {
                    firstFailure.addSuppressed(normalized);
                }
            }
        }

        if (firstFailure != null) {
            throw firstFailure;
        }
    }

    /**
     * Converts a checked cleanup failure into a runtime failure while preserving
     * existing runtime exceptions unchanged.
     *
     * @param failure cleanup failure raised by {@link AutoCloseable#close()}.
     * @return runtime representation suitable for JUnit lifecycle propagation.
     */
    private static RuntimeException normalize(Exception failure) {
        return failure instanceof RuntimeException runtimeFailure
                ? runtimeFailure
                : new RuntimeException("Managed test resource failed to close.", failure);
    }
}
