package com.bavykin.qualistrata.core.config.resolution;

import com.bavykin.qualistrata.core.config.loader.TargetConfigLoader;
import com.bavykin.qualistrata.core.config.merge.TargetConfigMerger;
import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.model.TargetConfig;
import com.bavykin.qualistrata.core.config.path.TargetConfigPathResolver;
import com.bavykin.qualistrata.core.config.path.TargetConfigPaths;
import com.bavykin.qualistrata.core.config.selection.SelectedTarget;
import com.bavykin.qualistrata.core.config.selection.TargetSelector;
import com.bavykin.qualistrata.core.exceptions.FrameworkException;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves the effective target configuration for a framework run.
 *
 * <p>This class orchestrates generic QualiStrata core primitives:</p>
 *
 * <ol>
 *     <li>select active target name;</li>
 *     <li>resolve conventional configuration paths;</li>
 *     <li>load the mandatory public target YAML;</li>
 *     <li>optionally load local secrets layers;</li>
 *     <li>merge all loaded layers into one effective {@link ResolvedTarget}.</li>
 * </ol>
 *
 * <p>The resolver intentionally does not define product-specific target names,
 * does not know customer environments, and does not contain a hardcoded default
 * such as {@code release_6.6}. If a project wants a default target, it must
 * pass it explicitly through {@link TargetConfigResolutionRequest}.</p>
 */
public class TargetConfigResolver {

    private final TargetSelector targetSelector;
    private final TargetConfigPathResolver pathResolver;
    private final TargetConfigLoader configLoader;
    private final TargetConfigMerger configMerger;

    /**
     * Creates a target configuration resolver with default core components.
     */
    public TargetConfigResolver() {
        this(
                new TargetSelector(),
                new TargetConfigPathResolver(),
                new TargetConfigLoader(),
                new TargetConfigMerger()
        );
    }

    /**
     * Creates a target configuration resolver with explicitly provided
     * collaborators.
     *
     * <p>This constructor keeps the resolver testable and allows future
     * customization without changing the public resolution API.</p>
     *
     * @param targetSelector selects the active target name.
     * @param pathResolver resolves conventional configuration paths.
     * @param configLoader loads YAML files into target configuration objects.
     * @param configMerger merges target configuration layers.
     */
    TargetConfigResolver(
            TargetSelector targetSelector,
            TargetConfigPathResolver pathResolver,
            TargetConfigLoader configLoader,
            TargetConfigMerger configMerger
    ) {
        this.targetSelector = targetSelector;
        this.pathResolver = pathResolver;
        this.configLoader = configLoader;
        this.configMerger = configMerger;
    }

    /**
     * Resolves the effective target configuration from the provided request.
     *
     * <p>The public target configuration file is mandatory. Default secrets,
     * target-specific secrets, and explicit secrets files are optional and are
     * merged only when they exist.</p>
     *
     * @param request resolution request containing project root and runtime
     *                target selection inputs.
     * @return resolved target name and effective merged configuration.
     * @throws FrameworkException if the request is missing or mandatory target
     *                            configuration cannot be loaded.
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

        TargetConfig resolvedConfig = configLoader.load(paths.targetConfigPath());
        resolvedConfig = mergeOptionalLayer(resolvedConfig, paths.defaultSecretsPath());
        resolvedConfig = mergeOptionalLayer(resolvedConfig, paths.targetSecretsPath());
        resolvedConfig = mergeOptionalLayer(resolvedConfig, paths.explicitSecretsPath());

        return new ResolvedTarget(selectedTarget.name(), resolvedConfig);
    }

    /**
     * Loads and merges an optional configuration layer when the file exists.
     *
     * <p>Optional layers are skipped when their path is {@code null} or when the
     * file does not exist. This allows projects to use local secrets files only
     * when they are needed.</p>
     *
     * @param current currently resolved configuration.
     * @param optionalPath optional configuration layer path.
     * @return current configuration merged with the optional layer when present.
     */
    private TargetConfig mergeOptionalLayer(TargetConfig current, Path optionalPath) {
        if (optionalPath == null || !Files.isRegularFile(optionalPath)) {
            return current;
        }

        TargetConfig layer = configLoader.load(optionalPath);

        return configMerger.merge(current, layer);
    }
}