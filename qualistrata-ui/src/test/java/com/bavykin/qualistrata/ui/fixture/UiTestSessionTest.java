package com.bavykin.qualistrata.ui.fixture;

import com.bavykin.qualistrata.ui.driver.DownloadSupport;
import com.bavykin.qualistrata.ui.driver.UiBrowserContext;
import com.bavykin.qualistrata.ui.driver.UiCapabilities;
import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.driver.WindowSupport;
import com.bavykin.qualistrata.ui.element.UiElement;
import com.bavykin.qualistrata.ui.element.UiElementCollection;
import com.bavykin.qualistrata.ui.locator.UiLocator;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/** Unit tests for context/session lifecycle handle. */
public class UiTestSessionTest {

    @Test
    public void closesSessionBeforeContext() {
        order = 0;
        RecordingSession session = new RecordingSession();
        RecordingContext context = new RecordingContext(session);
        UiTestSession testSession = new UiTestSession(context, session);

        testSession.close();

        assertEquals(session.closeOrder, 1);
        assertEquals(context.closeOrder, 2);
        assertTrue(testSession.closed());
    }

    @Test
    public void keepsContextFailureSuppressedWhenBothCloseOperationsFail() {
        order = 0;
        RecordingSession session = new RecordingSession();
        RecordingContext context = new RecordingContext(session);
        session.failure = new RuntimeException("session close failed");
        context.failure = new RuntimeException("context close failed");
        UiTestSession testSession = new UiTestSession(context, session);

        RuntimeException thrown = expectThrows(RuntimeException.class, testSession::close);

        assertSame(thrown, session.failure);
        assertEquals(thrown.getSuppressed().length, 1);
        assertSame(thrown.getSuppressed()[0], context.failure);
        assertTrue(testSession.closed());
    }

    private static int order;

    private static final class RecordingContext implements UiBrowserContext {
        private final RecordingSession session;
        private int closeOrder;
        private RuntimeException failure;

        private RecordingContext(RecordingSession session) {
            this.session = session;
        }

        @Override
        public UiSession newSession() {
            return session;
        }

        @Override
        public void close() {
            closeOrder = ++order;
            if (failure != null) {
                throw failure;
            }
        }
    }

    private static final class RecordingSession implements UiSession {
        private int closeOrder;
        private RuntimeException failure;

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
            throw new UnsupportedOperationException("Not needed by this unit test.");
        }

        @Override
        public UiElementCollection elements(UiLocator locator) {
            throw new UnsupportedOperationException("Not needed by this unit test.");
        }

        @Override
        public WindowSupport windows() {
            throw new UnsupportedOperationException("Not needed by this unit test.");
        }

        @Override
        public DownloadSupport downloads() {
            throw new UnsupportedOperationException("Not needed by this unit test.");
        }

        @Override
        public UiCapabilities capabilities() {
            return UiCapabilities.SELENIUM_BASIC;
        }

        @Override
        public void close() {
            closeOrder = ++order;
            if (failure != null) {
                throw failure;
            }
        }
    }
}
