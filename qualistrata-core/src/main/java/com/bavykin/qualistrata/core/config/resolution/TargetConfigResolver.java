package com.bavykin.qualistrata.core.config.resolution;

import com.bavykin.qualistrata.core.config.loader.SecretsConfigLoader;
import com.bavykin.qualistrata.core.config.loader.TargetConfigLoader;
import com.bavykin.qualistrata.core.config.merge.SecretsConfigMerger;
import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.config.path.TargetConfigPathResolver;
import com.bavykin.qualistrata.core.config.path.TargetConfigPaths;
import com.bavykin.qualistrata.core.config.secrets.SecretsConfig;
import com.bavykin.qualistrata.core.config.selection.SelectedTarget;
import com.bavykin.qualistrata.core.config.selection.TargetSelector;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves target topology and secrets for a framework run.
 *
 * <p>The resolver intentionally loads {@link TargetConfig} and
 * {@link SecretsConfig} as separate models. Secrets are not merged into target
 * resources. Runtime sessions and clients must use an access resolver to obtain
 * a concrete resolved access configuration.</p>
 */
public class TargetConfigResolver {

    private final TargetSelector targetSelector;
    private final TargetConfigPathResolver pathResolver;
    private final TargetConfigLoader targetConfigLoader;
    private final SecretsConfigLoader secretsConfigLoader;
    private final SecretsConfigMerger secretsConfigMerger;

    /** Creates a target configuration resolver with default core components. */
    public TargetConfigResolver() {
        this(
                new TargetSelector(),
                new TargetConfigPathResolver(),
                new TargetConfigLoader(),
                new SecretsConfigLoader(),
                new SecretsConfigMerger()
        );
    }

    TargetConfigResolver(
            TargetSelector targetSelector,
            TargetConfigPathResolver pathResolver,
            TargetConfigLoader targetConfigLoader,
            SecretsConfigLoader secretsConfigLoader,
            SecretsConfigMerger secretsConfigMerger
    ) {
        this.targetSelector = targetSelector;
        this.pathResolver = pathResolver;
        this.targetConfigLoader = targetConfigLoader;
        this.secretsConfigLoader = secretsConfigLoader;
        this.secretsConfigMerger = secretsConfigMerger;
    }

    /**
     * Resolves target topology and optional secrets layers.
     *
     * @param request resolution request.
     * @return selected target with separate target config and secrets config.
     */
    public ResolvedTarget resolve(TargetConfigResolutionRequest request) {
        if (request == null) {
            throw new FrameworkException("Target configuration resolution request must not be null.");
        }

        SelectedTarget selectedTarget = targetSelector.select(
                request.properties(),
                request.environment(),
                request.projectDefaultTarget()
        );

        TargetConfigPaths paths = pathResolver.resolve(
                request.projectRoot(),
                selectedTarget.name(),
                request.explicitSecretsPath()
        );

        TargetConfig targetConfig = targetConfigLoader.load(paths.targetConfigPath());
        SecretsConfig secretsConfig = null;
        secretsConfig = mergeOptionalSecretsLayer(secretsConfig, paths.defaultSecretsPath());
        secretsConfig = mergeOptionalSecretsLayer(secretsConfig, paths.targetSecretsPath());
        secretsConfig = mergeOptionalSecretsLayer(secretsConfig, paths.explicitSecretsPath());

        return new ResolvedTarget(selectedTarget.name(), targetConfig, secretsConfig);
    }

    private SecretsConfig mergeOptionalSecretsLayer(SecretsConfig current, Path optionalPath) {
        if (optionalPath == null || !Files.isRegularFile(optionalPath)) {
            return current;
        }

        SecretsConfig layer = secretsConfigLoader.load(optionalPath);

        return secretsConfigMerger.merge(current, layer);
    }
}
