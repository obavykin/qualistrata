package com.bavykin.qualistrata.report.metadata;

import java.util.Map;

/**
 * Supplies consumer-specific metadata for one test run.
 *
 * <p>Implementations may be registered through {@link java.util.ServiceLoader}.
 * Returned keys must be non-blank. Values should be JSON-safe scalar values,
 * maps, collections, or arrays because the canonical report format is JSON.</p>
 */
@FunctionalInterface
public interface TestRunMetadataProvider {

    /**
     * Returns metadata contributed by this provider.
     *
     * @return provider metadata; never {@code null}
     */
    Map<String, Object> metadata();
}
