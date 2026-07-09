package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.artifact.UiArtifact;
import com.bavykin.qualistrata.ui.artifact.UiArtifactCollector;
import com.bavykin.qualistrata.ui.artifact.UiArtifactType;
import com.bavykin.qualistrata.ui.driver.UiSession;
import com.microsoft.playwright.Tracing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Collects a Playwright trace for failed tests.
 *
 * <p>Trace recording is started when a Playwright browser context is created
 * and UI trace mode is not {@code OFF}. This collector stops the context trace
 * and stores it as a zip file. It is safe to use in a composite collector: when
 * tracing is unsupported or already stopped, it returns no artifacts.</p>
 */
public final class PlaywrightTraceArtifactCollector implements UiArtifactCollector {

    private final Path outputDirectory;

    /** Creates collector. */
    public PlaywrightTraceArtifactCollector(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public List<UiArtifact> collectOnFailure(UiSession session, String testName, Throwable failure) {
        if (!(session instanceof PlaywrightUiSession playwrightSession)) {
            return List.of();
        }

        try {
            Files.createDirectories(outputDirectory);
            Path trace = outputDirectory.resolve(safeName(testName) + ".zip");
            playwrightSession.nativePage()
                    .context()
                    .tracing()
                    .stop(new Tracing.StopOptions().setPath(trace));
            return List.of(new UiArtifact(UiArtifactType.TRACE, trace, Instant.now()));
        } catch (RuntimeException | java.io.IOException exception) {
            return List.of();
        }
    }

    private String safeName(String value) {
        if (value == null || value.isBlank()) {
            return "ui-test";
        }

        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
