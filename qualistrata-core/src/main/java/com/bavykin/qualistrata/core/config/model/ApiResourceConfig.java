package com.bavykin.qualistrata.core.config.model;

/**
 * Generic API resource configuration placeholder.
 *
 * <p>The model intentionally starts small. It gives QualiStrata a stable place
 * for future API endpoint resolution without coupling core to a specific
 * protocol, authentication scheme, or consumer project.</p>
 *
 * @param type optional API type or protocol family.
 * @param baseUrl base URL of the API endpoint.
 * @param defaultAccess default access profile alias.
 */
public record ApiResourceConfig(
        String type,
        String baseUrl,
        String defaultAccess
) {
}
