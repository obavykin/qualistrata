package com.bavykin.qualistrata.ui.artifact;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositeUiArtifactCollectorTest {

    @Test
    void keepsCollectingArtifactsWhenOneCollectorFails() {
        UiArtifact expected = new UiArtifact(UiArtifactType.SCREENSHOT, Path.of("screenshot.png"), Instant.now());
        CompositeUiArtifactCollector collector = new CompositeUiArtifactCollector(List.of(
                (session, testName, failure) -> { throw new RuntimeException("broken collector"); },
                (session, testName, failure) -> List.of(expected)
        ));

        List<UiArtifact> artifacts = collector.collectOnFailure(null, "test", new AssertionError("failed"));

        assertEquals(List.of(expected), artifacts);
    }
}
