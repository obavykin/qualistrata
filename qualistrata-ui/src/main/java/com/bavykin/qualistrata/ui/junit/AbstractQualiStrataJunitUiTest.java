package com.bavykin.qualistrata.ui.junit;

import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.fixture.UiTestRuntime;
import com.bavykin.qualistrata.ui.fixture.UiTestSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Optional JUnit 5 base class for consumer UI tests.
 *
 * <p>Each test method owns its own UI runtime, browser, context and session.
 * This keeps Playwright objects confined to the JUnit execution thread that
 * created them and allows methods from the same test class to run concurrently
 * without sharing a Playwright transport. Product authentication and business
 * page objects remain in consumer projects.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(QualiStrataJunitUiExtension.class)
public abstract class AbstractQualiStrataJunitUiTest {

    /**
     * Creates a JUnit UI test fixture.
     */
    public AbstractQualiStrataJunitUiTest() {
    }

    private final ThreadLocal<UiTestRuntime> currentRuntime = new ThreadLocal<>();
    private final ThreadLocal<UiTestSession> currentTestSession = new ThreadLocal<>();
    private final ThreadLocal<ManagedTestResources> currentManagedResources = new ThreadLocal<>();

    /**
     * Supplies effective UI runtime configuration.
     *
     * @return the resulting value.
     */
    protected abstract UiRuntimeConfig uiRuntimeConfig();

    /**
     * Creates a thread-confined runtime, browser context/session and managed-resource
     * registry for the next test method.
     *
     * <p>Playwright Java objects are not shared between concurrent JUnit methods.
     * The runtime is created and closed on the same execution thread as the test.
     * The registry is created before consumer test code executes, so any
     * {@link AutoCloseable} returned by a page flow may be registered through
     * {@link #manage(AutoCloseable)} immediately after it is opened.</p>
     */
    @BeforeEach
    void setUpQualiStrataUiSession() {
        UiTestRuntime runtime = UiTestRuntime.create(uiRuntimeConfig());
        try {
            currentRuntime.set(runtime);
            currentTestSession.set(runtime.newTestSession());
            currentManagedResources.set(new ManagedTestResources());
        } catch (RuntimeException | Error failure) {
            currentRuntime.remove();
            runtime.close();
            throw failure;
        }
    }

    /**
     * Closes all resources registered by the current test and then closes the
     * isolated UI test session.
     *
     * <p>Managed resources are closed in reverse registration order. This mirrors
     * nested acquisition order and ensures child windows, dialogs, temporary
     * sessions and similar resources are released before their owning browser
     * context. Cleanup continues after an individual close failure; subsequent
     * failures are attached as suppressed exceptions to the first failure.</p>
     *
     * <p>The browser runtime is closed last and on the same JUnit execution thread
     * that created it. Thread-local state is removed before cleanup starts, so a
     * failing resource cannot leak lifecycle state into a later test method.</p>
     */
    @AfterEach
    void tearDownQualiStrataUiSession() {
        ManagedTestResources managedResources = currentManagedResources.get();
        UiTestSession testSession = currentTestSession.get();
        UiTestRuntime runtime = currentRuntime.get();

        currentManagedResources.remove();
        currentTestSession.remove();
        currentRuntime.remove();

        RuntimeException cleanupFailure = null;

        if (managedResources != null) {
            try {
                managedResources.close();
            } catch (RuntimeException failure) {
                cleanupFailure = failure;
            }
        }

        if (testSession != null) {
            try {
                testSession.close();
            } catch (RuntimeException failure) {
                if (cleanupFailure == null) {
                    cleanupFailure = failure;
                } else {
                    cleanupFailure.addSuppressed(failure);
                }
            }
        }

        if (runtime != null) {
            try {
                runtime.close();
            } catch (RuntimeException failure) {
                if (cleanupFailure == null) {
                    cleanupFailure = failure;
                } else {
                    cleanupFailure.addSuppressed(failure);
                }
            }
        }

        if (cleanupFailure != null) {
            throw cleanupFailure;
        }
    }

    /**
     * Registers a test-owned resource for automatic cleanup after the current
     * test method.
     *
     * <p>The same resource instance is returned so registration can be embedded
     * directly into fluent setup code:</p>
     *
     * <pre>{@code
     * ReportFlow report = manage(reportDsl.open());
     * }</pre>
     *
     * <p>Resources are closed in reverse registration order before the isolated
     * UI session is closed. Register only resources owned by the current test.
     * Shared class-level runtimes and objects managed elsewhere must not be
     * registered here.</p>
     *
     * @param resource non-null resource owned by the current test method.
     * @param <T> concrete resource type.
     * @return the same resource instance.
     * @throws NullPointerException when {@code resource} is {@code null}.
     * @throws IllegalStateException when called outside an active test-method lifecycle.
     */
    protected final <T extends AutoCloseable> T manage(T resource) {
        ManagedTestResources managedResources = currentManagedResources.get();
        if (managedResources == null) {
            throw new IllegalStateException(
                    "QualiStrata managed-resource lifecycle has not been initialised "
                            + "for the current test method."
            );
        }
        return managedResources.register(resource);
    }

    /**
     * Returns the active thread-confined UI runtime.
     *
     * @return active runtime for advanced consumer fixture code.
     */
    protected UiTestRuntime runtime() {
        UiTestRuntime runtime = currentRuntime.get();
        if (runtime == null) {
            throw new IllegalStateException(
                    "QualiStrata UI runtime has not been initialised for the current test method."
            );
        }
        return runtime;
    }

    /**
     * Returns the current test lifecycle handle.
     *
     * @return current test lifecycle handle.
     */
    protected UiTestSession testSession() {
        UiTestSession testSession = currentTestSession.get();
        if (testSession == null) {
            throw new IllegalStateException("QualiStrata UI session has not been initialised for the current test method.");
        }
        return testSession;
    }

    /**
     * Returns the primary UI session for the current test method.
     *
     * @return primary UI session/page for the current test method.
     */
    @SuppressWarnings("resource")
    protected UiSession session() {
        return testSession().session();
    }
}
