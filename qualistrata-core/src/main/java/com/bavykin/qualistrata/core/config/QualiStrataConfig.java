package com.bavykin.qualistrata.core.config;

import com.bavykin.qualistrata.core.config.model.ResolvedTarget;
import com.bavykin.qualistrata.core.config.resolution.TargetConfigResolutionRequest;
import com.bavykin.qualistrata.core.config.resolution.TargetConfigResolver;
import com.bavykin.qualistrata.core.config.validation.TargetConfigValidator;

/**
 * Facade for resolving and validating QualiStrata target configuration.
 *
 * <p>This class is the main entry point for consuming projects that need an
 * effective {@link ResolvedTarget}. It hides the lower-level orchestration
 * details behind a small and stable API.</p>
 *
 * <p>The facade intentionally combines only generic core behavior:</p>
 *
 * <ol>
 *     <li>resolve target configuration layers;</li>
 *     <li>validate generic target configuration structure;</li>
 *     <li>return the validated resolved target.</li>
 * </ol>
 *
 * <p>Product-specific validation, environment rules, credential checks,
 * connectivity checks, and client/session creation must remain outside this
 * facade.</p>
 */
public class QualiStrataConfig {

    private final TargetConfigResolver resolver;
    private final TargetConfigValidator validator;

    /**
     * Creates a configuration facade with default core components.
     */
    public QualiStrataConfig() {
        this(
                new TargetConfigResolver(),
                new TargetConfigValidator()
        );
    }

    /**
     * Creates a configuration facade with explicitly provided collaborators.
     *
     * <p>This constructor keeps the facade testable and allows future extension
     * without forcing consuming projects to use static global state.</p>
     *
     * @param resolver resolves effective target configuration.
     * @param validator validates resolved target configuration structure.
     */
    QualiStrataConfig(
            TargetConfigResolver resolver,
            TargetConfigValidator validator
    ) {
        this.resolver = resolver;
        this.validator = validator;
    }

    /**
     * Resolves and validates the effective target configuration.
     *
     * <p>The method first delegates to {@link TargetConfigResolver}. The
     * resolved target is then validated by {@link TargetConfigValidator}. If
     * validation succeeds, the same resolved target is returned to the caller.</p>
     *
     * @param request target configuration resolution request.
     * @return validated resolved target configuration.
     */
    public ResolvedTarget resolveTarget(TargetConfigResolutionRequest request) {
        ResolvedTarget resolvedTarget = resolver.resolve(request);

        validator.validate(resolvedTarget);

        return resolvedTarget;
    }
}