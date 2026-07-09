package com.bavykin.qualistrata.ui.artifact;

import com.bavykin.qualistrata.ui.driver.UiSession;

import java.util.List;

/**
 * Collects diagnostic artifacts for a UI session.
 */
public interface UiArtifactCollector {

    /**
     * Collects artifacts for a failed test.
     *
     * @param session active session when available.
     * @param testName logical test name.
     * @param failure failure that triggered collection.
     * @return collected artifact descriptors.
     */
    List<UiArtifact> collectOnFailure(UiSession session, String testName, Throwable failure);
}
