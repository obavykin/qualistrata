package com.bavykin.qualistrata.core.config.secrets;

import java.util.Map;

/**
 * Secret access profiles grouped by generic resource type and resource alias.
 *
 * <p>The shape is always:</p>
 *
 * <pre>
 * access.&lt;resource-type&gt;.&lt;resource-alias&gt;.&lt;access-alias&gt;
 * </pre>
 *
 * <p>Consumer projects choose aliases. QualiStrata core only resolves the
 * relationship between a resource alias and an access alias.</p>
 *
 * @param ui UI login access profiles.
 * @param unix Unix/SSH access profiles.
 * @param databases database access profiles.
 * @param api API access profiles.
 * @param metrics metrics backend access profiles.
 */
public record AccessSecretsConfig(
        Map<String, Map<String, UiAccessSecret>> ui,
        Map<String, Map<String, UnixAccessSecret>> unix,
        Map<String, Map<String, DatabaseAccessSecret>> databases,
        Map<String, Map<String, ApiAccessSecret>> api,
        Map<String, Map<String, MetricsAccessSecret>> metrics
) {
}
