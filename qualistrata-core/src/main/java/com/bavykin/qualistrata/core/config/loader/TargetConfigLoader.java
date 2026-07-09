package com.bavykin.qualistrata.core.config.loader;

import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads a {@link TargetConfig} from a YAML configuration file.
 *
 * <p>This loader is intentionally generic. It does not know about any specific
 * product, environment naming convention, default target, or repository layout.
 * Project-specific modules are responsible for deciding which target file
 * should be loaded and where it is located.</p>
 */
public class TargetConfigLoader {

    private final ObjectMapper objectMapper;

    /**
     * Creates a target configuration loader backed by a YAML object mapper.
     */
    public TargetConfigLoader() {
        this(new ObjectMapper(new YAMLFactory()));
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
        this.objectMapper = objectMapper;
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

        try {
            return objectMapper.readValue(path.toFile(), TargetConfig.class);
        } catch (IOException e) {
            throw new FrameworkException("Failed to load target configuration from: " + path, e);
        }
    }
}