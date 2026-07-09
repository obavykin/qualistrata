package com.bavykin.qualistrata.core.environment;

/**
 * Generic contract for an environment readiness check.
 *
 * <p>An environment check should validate one specific technical or logical
 * readiness condition and return an {@link EnvironmentCheckResult}. Checks must
 * not throw framework exceptions for expected environment problems; such
 * problems should normally be represented as failed check results.</p>
 */
@FunctionalInterface
public interface EnvironmentCheck {

    /**
     * Executes the environment check.
     *
     * @return environment check result.
     */
    EnvironmentCheckResult check();
}