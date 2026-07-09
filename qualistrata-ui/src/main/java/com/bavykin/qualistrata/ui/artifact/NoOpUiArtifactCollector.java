package com.bavykin.qualistrata.ui.artifact;

import com.bavykin.qualistrata.ui.driver.UiSession;

import java.util.List;

/**
 * Artifact collector used when artifact collection is disabled or unsupported.
 */
public final class NoOpUiArtifactCollector implements UiArtifactCollector {

    /** Shared no-op instance. */
    public static final NoOpUiArtifactCollector INSTANCE = new NoOpUiArtifactCollector();

    private NoOpUiArtifactCollector() {
    }

    @Override
    public List<UiArtifact> collectOnFailure(UiSession session, String testName, Throwable failure) {
        return List.of();
    }
}
