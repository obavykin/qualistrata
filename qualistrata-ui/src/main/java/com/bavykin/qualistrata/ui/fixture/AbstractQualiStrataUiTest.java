package com.bavykin.qualistrata.ui.fixture;

import com.bavykin.qualistrata.ui.config.UiRuntimeConfig;
import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.testng.QualiStrataUiTestListener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * Optional TestNG base class for consumer UI tests.
 *
 * <p>The base class provides the standard QualiStrata lifecycle pattern: one
 * heavy UI runtime per test class and one isolated browser context/session per
 * test method. Consumer projects may extend this class directly or copy the
 * same pattern into their own richer product-specific base class.</p>
 *
 * <p>The class deliberately does not resolve targets by itself and does not
 * perform login. Subclasses decide how {@link UiRuntimeConfig} is created and
 * how product authentication is handled.</p>
 */
@Listeners(QualiStrataUiTestListener.class)
public abstract class AbstractQualiStrataUiTest {

    /**
     * Creates a TestNG UI test fixture.
     */
    public AbstractQualiStrataUiTest() {
    }

    private UiTestRuntime runtime;
    private final ThreadLocal<UiTestSession> currentTestSession = new ThreadLocal<>();

    /**
     * Supplies effective UI runtime configuration.
     *
     * <p>Typical consumer implementations create this from a resolved
     * {@code ResolvedUiConfig} using {@link UiTestRuntime#fromSystemProperties} or
     * {@code UiRuntimeConfigFactory.from(...)}.</p>
     *
     * @return the resulting value.
     */
    protected abstract UiRuntimeConfig uiRuntimeConfig();

    /** Creates the heavy browser runtime once for this TestNG class instance. */
    @BeforeClass(alwaysRun = true)
    public void setUpQualiStrataUiRuntime() {
        runtime = UiTestRuntime.create(uiRuntimeConfig());
    }

    /** Creates a fresh isolated context/session for the next test method. */
    @BeforeMethod(alwaysRun = true)
    public void setUpQualiStrataUiSession() {
        currentTestSession.set(runtime().newTestSession());
    }

    /** Closes the current test context/session even when the test failed. */
    @AfterMethod(alwaysRun = true)
    public void tearDownQualiStrataUiSession() {
        UiTestSession testSession = currentTestSession.get();
        currentTestSession.remove();
        if (testSession != null) {
            testSession.close();
        }
    }

    /** Closes the heavy browser runtime after all methods in this class. */
    @AfterClass(alwaysRun = true)
    public void tearDownQualiStrataUiRuntime() {
        if (runtime != null) {
            runtime.close();
            runtime = null;
        }
    }

    /**
     * Returns the active UI runtime.
     *
     * @return active runtime for advanced consumer fixture code.
     */
    protected UiTestRuntime runtime() {
        if (runtime == null) {
            throw new IllegalStateException("QualiStrata UI runtime has not been initialised yet.");
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
    protected UiSession session() {
        return testSession().session();
    }
}
