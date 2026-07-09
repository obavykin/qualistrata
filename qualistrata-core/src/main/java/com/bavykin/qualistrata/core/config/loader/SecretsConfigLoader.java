package com.bavykin.qualistrata.core.config.loader;

import com.bavykin.qualistrata.core.config.secrets.SecretsConfig;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads a {@link SecretsConfig} from a YAML secrets file.
 *
 * <p>Secrets are parsed separately from target topology. This keeps
 * QualiStrata resource configuration generic and prevents secret values from
 * being merged into public topology models.</p>
 */
public class SecretsConfigLoader {

    private final ObjectMapper objectMapper;

    /**
     * Creates a secrets configuration loader backed by a YAML object mapper.
     */
    public SecretsConfigLoader() {
        this(new ObjectMapper(new YAMLFactory()));
    }

    /**
     * Creates a secrets configuration loader with an explicitly provided mapper.
     *
     * @param objectMapper mapper used to deserialize YAML content.
     */
    SecretsConfigLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Loads secrets configuration from the provided YAML file.
     *
     * @param path path to a secrets YAML file.
     * @return parsed secrets configuration.
     */
    public SecretsConfig load(Path path) {
        if (path == null) {
            throw new FrameworkException("Secrets configuration path must not be null.");
        }

        if (!Files.isRegularFile(path)) {
            throw new FrameworkException("Secrets configuration file does not exist: " + path);
        }

        try {
            return objectMapper.readValue(path.toFile(), SecretsConfig.class);
        } catch (IOException e) {
            throw new FrameworkException("Failed to load secrets configuration from: " + path, e);
        }
    }
}
