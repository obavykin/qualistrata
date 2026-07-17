package com.bavykin.qualistrata.report.junit;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import java.util.Objects;

/**
 * Registers the QualiStrata JSON report listener for each JUnit launcher session.
 *
 * <p>This integration path is used because Gradle may create a JUnit launcher
 * with automatic {@code TestExecutionListener} discovery disabled. Launcher
 * session listeners are loaded earlier and can register the report listener
 * directly with the active launcher.</p>
 */
public final class JUnitJsonReportLauncherSessionListener
        implements LauncherSessionListener {

    /**
     * Creates a launcher-session listener.
     */
    public JUnitJsonReportLauncherSessionListener() {
    }

    /**
     * Registers a new JSON report listener with the opened launcher session.
     *
     * @param session opened JUnit launcher session
     */
    @Override
    public void launcherSessionOpened(LauncherSession session) {
        Objects.requireNonNull(session, "session");
        session.getLauncher().registerTestExecutionListeners(
                new JUnitJsonReportListener()
        );
    }
}
