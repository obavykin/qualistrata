package com.bavykin.qualistrata.ui.fixture;

import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.driver.DownloadSupport;
import com.bavykin.qualistrata.ui.driver.UiBrowserContext;
import com.bavykin.qualistrata.ui.driver.UiCapabilities;
import com.bavykin.qualistrata.ui.driver.UiDriver;
import com.bavykin.qualistrata.ui.driver.UiDriverFactory;
import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.driver.WindowSupport;
import com.bavykin.qualistrata.ui.element.UiElement;
import com.bavykin.qualistrata.ui.element.UiElementCollection;
import com.bavykin.qualistrata.ui.locator.UiLocator;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/** Unit tests for the reusable UI test runtime fixture. */
public class UiTestRuntimeTest {

    @Test
    public void createsIsolatedTestSessionFromDriverContext() {
        FakeDriverFactory factory = new FakeDriverFactory();
        UiRuntimeConfig config = UiRuntimeConfig.defaults("https://example.test");

        UiTestRuntime runtime = UiTestRuntime.create(config, factory);
        UiTestSession testSession = runtime.newTestSession();

        assertSame(runtime.config(), config);
        assertEquals(factory.driver.contextsCreated, 1);
        assertTrue(testSession.session() instanceof FakeSession);
        assertFalse(testSession.closed());

        testSession.close();

        FakeContext context = factory.driver.lastContext;
        FakeSession session = context.lastSession;
        assertTrue(session.closed);
        assertTrue(context.closed);
        assertTrue(testSession.closed());

        runtime.close();
        assertTrue(factory.driver.closed);
    }

    @Test
    public void closeIsIdempotent() {
        FakeDriverFactory factory = new FakeDriverFactory();
        UiTestRuntime runtime = UiTestRuntime.create(UiRuntimeConfig.defaults("https://example.test"), factory);

        runtime.close();
        runtime.close();

        assertEquals(factory.driver.closeCalls, 1);
    }

    private static final class FakeDriverFactory implements UiDriverFactory {
        private final FakeDriver driver = new FakeDriver();

        @Override
        public UiDriver create(UiRuntimeConfig config) {
            return driver;
        }
    }

    private static final class FakeDriver implements UiDriver {
        private int contextsCreated;
        private int closeCalls;
        private boolean closed;
        private FakeContext lastContext;

        @Override
        public UiBrowserContext newContext() {
            contextsCreated++;
            lastContext = new FakeContext();
            return lastContext;
        }

        @Override
        public UiCapabilities capabilities() {
            return UiCapabilities.SELENIUM_BASIC;
        }

        @Override
        public void close() {
            closeCalls++;
            closed = true;
        }
    }

    private static final class FakeContext implements UiBrowserContext {
        private boolean closed;
        private FakeSession lastSession;

        @Override
        public UiSession newSession() {
            lastSession = new FakeSession();
            return lastSession;
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    private static final class FakeSession implements UiSession {
        private boolean closed;

        @Override
        public void open(String urlOrPath) {
        }

        @Override
        public String url() {
            return "https://example.test";
        }

        @Override
        public String title() {
            return "Example";
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
            closed = true;
        }
    }
}
