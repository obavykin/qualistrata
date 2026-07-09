package com.bavykin.qualistrata.ui.fixture;

import com.bavykin.qualistrata.core.config.resolved.ResolvedUiConfig;
import com.bavykin.qualistrata.ui.UiAutomationException;
import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.config.UiRuntimeConfigFactory;
import com.bavykin.qualistrata.ui.driver.DefaultUiDriverFactory;
import com.bavykin.qualistrata.ui.driver.UiBrowserContext;
import com.bavykin.qualistrata.ui.driver.UiDriver;
import com.bavykin.qualistrata.ui.driver.UiDriverFactory;
import com.bavykin.qualistrata.ui.driver.UiSession;

import java.util.Properties;

/**
 * Reusable runtime fixture for UI tests.
 *
 * <p>The runtime owns the heavy browser-engine driver and creates isolated
 * context/session pairs for individual tests. It is generic by design: product
 * authentication, page objects, roles, and business navigation stay in consumer
 * projects.</p>
 */
public final class UiTestRuntime implements AutoCloseable {

    private final UiRuntimeConfig config;
    private final UiDriver driver;
    private boolean closed;

    private UiTestRuntime(UiRuntimeConfig config, UiDriver driver) {
        this.config = config;
        this.driver = driver;
    }

    /** Creates a runtime using the default engine-switching driver factory. */
    public static UiTestRuntime create(UiRuntimeConfig config) {
        return create(config, new DefaultUiDriverFactory());
    }

    /** Creates a runtime using an explicit driver factory, useful for tests. */
    public static UiTestRuntime create(UiRuntimeConfig config, UiDriverFactory driverFactory) {
        if (config == null) {
            throw new IllegalArgumentException("UI runtime config must not be null.");
        }
        if (driverFactory == null) {
            throw new IllegalArgumentException("UI driver factory must not be null.");
        }

        UiDriver createdDriver = driverFactory.create(config);
        if (createdDriver == null) {
            throw new UiAutomationException("UI driver factory returned null driver.");
        }

        return new UiTestRuntime(config, createdDriver);
    }

    /** Creates a runtime from resolved core UI configuration and runtime overrides. */
    public static UiTestRuntime from(ResolvedUiConfig resolvedUiConfig, Properties properties) {
        return create(UiRuntimeConfigFactory.from(resolvedUiConfig, properties));
    }

    /** Creates a runtime from resolved core UI configuration using current system properties. */
    public static UiTestRuntime fromSystemProperties(ResolvedUiConfig resolvedUiConfig) {
        return from(resolvedUiConfig, System.getProperties());
    }

    /** @return effective UI runtime configuration. */
    public UiRuntimeConfig config() {
        return config;
    }

    /** @return underlying driver for advanced consumer fixtures. */
    public UiDriver driver() {
        ensureOpen();
        return driver;
    }

    /**
     * Creates a new isolated context/session pair for one test method.
     *
     * @return lifecycle handle that must be closed by the test fixture.
     */
    public UiTestSession newTestSession() {
        ensureOpen();
        UiBrowserContext context = driver.newContext();
        UiSession session = context.newSession();
        return new UiTestSession(context, session);
    }

    /** Closes browser-engine resources owned by this runtime. */
    @Override
    public void close() {
        if (closed) {
            return;
        }

        try {
            driver.close();
        } finally {
            closed = true;
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new UiAutomationException("UI test runtime is already closed.");
        }
    }
}
