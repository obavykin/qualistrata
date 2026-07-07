package com.bavykin.qualistrata.ui.driver;

import com.bavykin.qualistrata.ui.element.UiElement;
import com.bavykin.qualistrata.ui.element.UiElementCollection;
import com.bavykin.qualistrata.ui.locator.UiLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiSessionRegistryTest {

    @AfterEach
    void cleanup() {
        UiSessionRegistry.closeAllForCurrentThread();
    }

    @Test
    void registersActiveSessionForCurrentThread() {
        StubSession first = new StubSession();
        StubSession second = new StubSession();

        UiSessionRegistry.register(first);
        UiSessionRegistry.register(second);

        assertEquals(Optional.of(second), UiSessionRegistry.activeSession());
        assertEquals(2, UiSessionRegistry.currentThreadSessions().size());
    }

    @Test
    void unregistersSessionWhenClosedByFixture() {
        StubSession session = new StubSession();

        UiSessionRegistry.register(session);
        UiSessionRegistry.unregister(session);

        assertFalse(UiSessionRegistry.activeSession().isPresent());
    }

    @Test
    void closesAllSessionsAndSuppressesCleanupFailures() {
        StubSession first = new StubSession();
        StubSession second = new StubSession();
        second.failOnClose = true;

        UiSessionRegistry.register(first);
        UiSessionRegistry.register(second);

        Optional<RuntimeException> cleanupFailure = UiSessionRegistry.closeAllForCurrentThread();

        assertTrue(first.closed);
        assertTrue(second.closed);
        assertTrue(cleanupFailure.isPresent());
        assertEquals(1, cleanupFailure.get().getSuppressed().length);
        assertFalse(UiSessionRegistry.activeSession().isPresent());
    }

    private static final class StubSession implements UiSession {
        private boolean closed;
        private boolean failOnClose;

        @Override
        public void open(String urlOrPath) {
        }

        @Override
        public String url() {
            return "";
        }

        @Override
        public String title() {
            return "";
        }

        @Override
        public UiElement element(UiLocator locator) {
            return null;
        }

        @Override
        public UiElementCollection elements(UiLocator locator) {
            return null;
        }

        @Override
        public WindowSupport windows() {
            return null;
        }

        @Override
        public DownloadSupport downloads() {
            return null;
        }

        @Override
        public UiCapabilities capabilities() {
            return UiCapabilities.PLAYWRIGHT;
        }

        @Override
        public void close() {
            closed = true;
            if (failOnClose) {
                throw new RuntimeException("close failed");
            }
        }
    }
}
