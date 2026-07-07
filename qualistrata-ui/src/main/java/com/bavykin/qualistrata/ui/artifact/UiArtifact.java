package com.bavykin.qualistrata.ui.artifact;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Descriptor of a UI diagnostic artifact generated during test execution.
 *
 * @param type artifact type.
 * @param path local artifact path.
 * @param createdAt creation timestamp.
 */
public record UiArtifact(
        UiArtifactType type,
        Path path,
        Instant createdAt
) {
    /** Validates artifact descriptor. */
    public UiArtifact {
        if (type == null) {
            throw new IllegalArgumentException("UI artifact type must not be null.");
        }
        if (path == null) {
            throw new IllegalArgumentException("UI artifact path must not be null.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("UI artifact creation timestamp must not be null.");
        }
    }
}
