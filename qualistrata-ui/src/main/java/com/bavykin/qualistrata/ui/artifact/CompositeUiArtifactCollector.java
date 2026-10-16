package com.bavykin.qualistrata.ui.artifact;

import com.bavykin.qualistrata.ui.driver.UiSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes several artifact collectors as one failure-safe collector.
 *
 * <p>Artifact collection must never mask the original test failure. Therefore
 * exceptions thrown by individual collectors are intentionally ignored and the
 * remaining collectors are still executed.</p>
 */
public final class CompositeUiArtifactCollector implements UiArtifactCollector {

    private final List<UiArtifactCollector> collectors;

    /**
     * Creates collector from ordered delegates.
     *
     * @param collectors artifact collectors to invoke.
     */
    public CompositeUiArtifactCollector(List<UiArtifactCollector> collectors) {
        this.collectors = collectors == null ? List.of() : List.copyOf(collectors);
    }

    @Override
    public List<UiArtifact> collectOnFailure(UiSession session, String testName, Throwable failure) {
        List<UiArtifact> artifacts = new ArrayList<>();
        for (UiArtifactCollector collector : collectors) {
            try {
                artifacts.addAll(collector.collectOnFailure(session, testName, failure));
            } catch (RuntimeException ignored) {
                // Diagnostics are useful only when they are non-intrusive.
                // Keep the original functional failure authoritative.
            }
        }
        return List.copyOf(artifacts);
    }
}
