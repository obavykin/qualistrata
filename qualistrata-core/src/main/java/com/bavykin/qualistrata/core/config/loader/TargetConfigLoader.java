package com.bavykin.qualistrata.core.config.loader;

import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

/**
 * Loads a {@link TargetConfig} from a YAML configuration file.
 *
 * <p>This loader is intentionally generic. It does not know about any specific
 * product, environment naming convention, default target, or repository layout.
 * Project-specific modules are responsible for deciding which target file
 * should be loaded and where it is located.</p>
 */
public class TargetConfigLoader {

    private static final int DEFAULT_MAX_READ_ATTEMPTS = 20;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(50);

    private final ObjectMapper objectMapper;
    private final int maxReadAttempts;
    private final Duration retryDelay;

    /**
     * Creates a target configuration loader backed by a YAML object mapper.
     */
    public TargetConfigLoader() {
        this(new ObjectMapper(new YAMLFactory()), DEFAULT_MAX_READ_ATTEMPTS, DEFAULT_RETRY_DELAY);
    }

    /**
     * Creates a target configuration loader with an explicitly provided mapper.
     *
     * <p>This constructor is useful for unit tests and for future customization
     * of YAML parsing rules without changing the public loader API.</p>
     *
     * @param objectMapper mapper used to deserialize YAML content.
     */
    TargetConfigLoader(ObjectMapper objectMapper) {
        this(objectMapper, DEFAULT_MAX_READ_ATTEMPTS, DEFAULT_RETRY_DELAY);
    }

    TargetConfigLoader(ObjectMapper objectMapper, int maxReadAttempts, Duration retryDelay) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        if (maxReadAttempts < 1) {
            throw new IllegalArgumentException("maxReadAttempts must be at least 1.");
        }
        this.maxReadAttempts = maxReadAttempts;
        this.retryDelay = Objects.requireNonNull(retryDelay, "retryDelay");
        if (retryDelay.isNegative()) {
            throw new IllegalArgumentException("retryDelay must not be negative.");
        }
    }

    /**
     * Loads target configuration from the provided YAML file.
     *
     * @param path path to a target YAML file.
     * @return parsed target configuration.
     * @throws FrameworkException if the path is missing, not a regular file, or
     *                            cannot be parsed as {@link TargetConfig}.
     */
    public TargetConfig load(Path path) {
        if (path == null) {
            throw new FrameworkException("Target configuration path must not be null.");
        }

        if (!Files.isRegularFile(path)) {
            throw new FrameworkException("Target configuration file does not exist: " + path);
        }

        IOException lastFailure = null;
        for (int attempt = 1; attempt <= maxReadAttempts; attempt++) {
            try {
                return objectMapper.readValue(readStableSnapshot(path), TargetConfig.class);
            } catch (IOException e) {
                lastFailure = e;
                if (attempt < maxReadAttempts) {
                    awaitRetry(path);
                }
            }
        }

        throw new FrameworkException(
                "Failed to load target configuration from: " + path
                        + " after " + maxReadAttempts + " attempts.",
                lastFailure
        );
    }

    private byte[] readStableSnapshot(Path path) throws IOException {
        long sizeBefore = Files.size(path);
        long modifiedBefore = Files.getLastModifiedTime(path).toMillis();
        byte[] snapshot = Files.readAllBytes(path);
        long sizeAfter = Files.size(path);
        long modifiedAfter = Files.getLastModifiedTime(path).toMillis();

        if (snapshot.length == 0) {
            throw new IOException("Target configuration file is empty: " + path);
        }
        if (sizeBefore != sizeAfter || modifiedBefore != modifiedAfter || snapshot.length != sizeAfter) {
            throw new IOException("Target configuration file changed while being read: " + path);
        }
        return snapshot;
    }

    private void awaitRetry(Path path) {
        try {
            Thread.sleep(retryDelay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FrameworkException(
                    "Interrupted while waiting to reload target configuration from: " + path,
                    e
            );
        }
    }
}
