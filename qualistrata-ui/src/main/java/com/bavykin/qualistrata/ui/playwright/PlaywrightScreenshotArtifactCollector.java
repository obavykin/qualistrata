package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.artifact.UiArtifact;
import com.bavykin.qualistrata.ui.artifact.UiArtifactCollector;
import com.bavykin.qualistrata.ui.artifact.UiArtifactType;
import com.bavykin.qualistrata.ui.driver.UiSession;
import com.microsoft.playwright.Page;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Playwright screenshot collector.
 *
 * <p>This collector is intentionally narrow. Rich tracing/video/network
 * collectors can be added later without changing the generic artifact model.</p>
 */
public final class PlaywrightScreenshotArtifactCollector implements UiArtifactCollector {

    private final Path outputDirectory;

    /**
     * Creates collector.
     *
     * @param outputDirectory screenshot output directory.
     */
    public PlaywrightScreenshotArtifactCollector(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public List<UiArtifact> collectOnFailure(UiSession session, String testName, Throwable failure) {
        if (!(session instanceof PlaywrightUiSession playwrightSession)) {
            return List.of();
        }

        try {
            Files.createDirectories(outputDirectory);
            Path screenshot = outputDirectory.resolve(safeName(testName) + ".png");
            playwrightSession.nativePage().screenshot(new Page.ScreenshotOptions().setPath(screenshot).setFullPage(true));
            return List.of(new UiArtifact(UiArtifactType.SCREENSHOT, screenshot, Instant.now()));
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
