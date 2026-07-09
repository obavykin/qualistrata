package com.bavykin.qualistrata.ui.junit;

import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.fixture.UiTestRuntime;
import com.bavykin.qualistrata.ui.fixture.UiTestSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Optional JUnit 5 base class for consumer UI tests.
 *
 * <p>The base class provides the same lifecycle pattern as the TestNG fixture:
 * one heavy UI runtime per test class and one isolated browser context/session
 * per test method. Product authentication and business page objects remain in
 * consumer projects.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(QualiStrataJunitUiExtension.class)
public abstract class AbstractQualiStrataJunitUiTest {

    private UiTestRuntime runtime;
    private final ThreadLocal<UiTestSession> currentTestSession = new ThreadLocal<>();

    /** Supplies effective UI runtime configuration. */
    protected abstract UiRuntimeConfig uiRuntimeConfig();

    /** Creates the heavy browser runtime once for this JUnit class instance. */
    @BeforeAll
    void setUpQualiStrataUiRuntime() {
        runtime = UiTestRuntime.create(uiRuntimeConfig());
    }

    /** Creates a fresh isolated context/session for the next test method. */
    @BeforeEach
    void setUpQualiStrataUiSession() {
        currentTestSession.set(runtime().newTestSession());
    }

    /** Closes the current test context/session even when the test failed. */
    @AfterEach
    void tearDownQualiStrataUiSession() {
        UiTestSession testSession = currentTestSession.get();
        currentTestSession.remove();
        if (testSession != null) {
            testSession.close();
        }
    }

    /** Closes the heavy browser runtime after all methods in this class. */
    @AfterAll
    void tearDownQualiStrataUiRuntime() {
        if (runtime != null) {
            runtime.close();
            runtime = null;
        }
    }

    /** @return active runtime for advanced consumer fixture code. */
    protected UiTestRuntime runtime() {
        if (runtime == null) {
            throw new IllegalStateException("QualiStrata UI runtime has not been initialised yet.");
        }
        return runtime;
    }

    /** @return current test lifecycle handle. */
    protected UiTestSession testSession() {
        UiTestSession testSession = currentTestSession.get();
        if (testSession == null) {
            throw new IllegalStateException("QualiStrata UI session has not been initialised for the current test method.");
        }
        return testSession;
    }

    /** @return primary UI session/page for the current test method. */
    protected UiSession session() {
        return testSession().session();
    }
}
