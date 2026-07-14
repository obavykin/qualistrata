package com.bavykin.qualistrata.ui.fixture;

import com.bavykin.qualistrata.ui.driver.UiBrowserContext;
import com.bavykin.qualistrata.ui.driver.UiSession;

/**
 * Owns one isolated UI context and the primary session created for a test method.
 *
 * <p>The object is intentionally small and product-agnostic. Consumer tests can
 * use {@link #session()} for page objects while the fixture keeps lifecycle
 * ownership explicit. Closing the test session closes both the page/session and
 * the browser context, which prevents state leakage between parallel tests.</p>
 */
public final class UiTestSession implements AutoCloseable {

    private final UiBrowserContext context;
    private final UiSession session;
    private boolean closed;

    /**
     * Creates a lifecycle handle for one test-owned context/session pair.
     *
     * @param context browser context owned by this lifecycle handle.
     * @param session UI session owned by this lifecycle handle.
     */
    public UiTestSession(UiBrowserContext context, UiSession session) {
        if (context == null) {
            throw new IllegalArgumentException("UI browser context must not be null.");
        }
        if (session == null) {
            throw new IllegalArgumentException("UI session must not be null.");
        }

        this.context = context;
        this.session = session;
    }

    /**
     * Provides the context operation.
     *
     * @return isolated browser context created for the test.
     */
    public UiBrowserContext context() {
        return context;
    }

    /**
     * Returns the primary UI session for the current test method.
     *
     * @return primary UI session/page created for the test.
     */
    public UiSession session() {
        return session;
    }

    /**
     * Provides the closed operation.
     *
     * @return whether this handle has already been closed.
     */
    public boolean closed() {
        return closed;
    }

    /**
     * Closes the session first and then the owning context.
     *
     * <p>Both close operations are attempted. If both fail, the context failure
     * is added as suppressed to the session failure so teardown remains
     * diagnosable without hiding the first error.</p>
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }

        RuntimeException failure = null;
        try {
            session.close();
        } catch (RuntimeException exception) {
            failure = exception;
        }

        try {
            context.close();
        } catch (RuntimeException exception) {
            if (failure == null) {
                failure = exception;
            } else {
                failure.addSuppressed(exception);
            }
        } finally {
            closed = true;
        }

        if (failure != null) {
            throw failure;
        }
    }
}
