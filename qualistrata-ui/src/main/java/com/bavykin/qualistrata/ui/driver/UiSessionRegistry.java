package com.bavykin.qualistrata.ui.driver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Tracks UI sessions owned by the current test thread.
 *
 * <p>The registry is intentionally thread-local because UI tests are commonly
 * executed in parallel by TestNG worker threads. It lets lifecycle listeners
 * collect diagnostics from the active session and close leaked sessions after a
 * failed test without introducing a global browser/session singleton.</p>
 */
public final class UiSessionRegistry {

    private static final ThreadLocal<Deque<UiSession>> SESSIONS = ThreadLocal.withInitial(ArrayDeque::new);

    private UiSessionRegistry() {
    }

    /**
     * Registers a session for the current test thread.
     *
     * @param session session to track.
     * @return the same session for fluent factory usage.
     */
    public static UiSession register(UiSession session) {
        if (session == null) {
            throw new IllegalArgumentException("UI session must not be null.");
        }

        SESSIONS.get().push(session);
        return session;
    }

    /**
     * Removes a session from the current thread registry if it is present.
     *
     * @param session session to unregister.
     */
    public static void unregister(UiSession session) {
        if (session == null) {
            return;
        }

        Deque<UiSession> sessions = SESSIONS.get();
        sessions.remove(session);
        if (sessions.isEmpty()) {
            SESSIONS.remove();
        }
    }

    /**
     * Provides the activeSession operation.
     *
     * @return the most recently created active session for this test thread.
     */
    public static Optional<UiSession> activeSession() {
        Deque<UiSession> sessions = SESSIONS.get();
        return sessions.isEmpty() ? Optional.empty() : Optional.of(sessions.peek());
    }

    /**
     * Provides the currentThreadSessions operation.
     *
     * @return snapshot of sessions currently registered for this test thread.
     */
    public static List<UiSession> currentThreadSessions() {
        return List.copyOf(SESSIONS.get());
    }

    /**
     * Closes all sessions still registered for this test thread.
     *
     * <p>Cleanup is best-effort. All close operations are attempted, and any
     * thrown exceptions are suppressed into the first one so test teardown does
     * not hide the original functional assertion failure.</p>
     *
     * @return optional cleanup failure containing suppressed close errors.
     */
    public static Optional<RuntimeException> closeAllForCurrentThread() {
        Deque<UiSession> sessions = SESSIONS.get();
        List<UiSession> snapshot = new ArrayList<>(sessions);
        sessions.clear();
        SESSIONS.remove();

        RuntimeException cleanupFailure = null;
        for (UiSession session : snapshot) {
            try {
                session.close();
            } catch (RuntimeException exception) {
                if (cleanupFailure == null) {
                    cleanupFailure = new RuntimeException("Failed to close one or more UI sessions after test execution.");
                }
                cleanupFailure.addSuppressed(exception);
            }
        }

        return Optional.ofNullable(cleanupFailure);
    }
}
