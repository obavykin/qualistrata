package com.bavykin.qualistrata.core.config.model;

/**
 * UI-related target configuration.
 *
 * <p>This record contains generic settings required by UI automation modules to
 * connect to the application under test. Product-specific page objects,
 * navigation rules, credentials, and business flows must be defined outside the
 * core framework.</p>
 *
 * @param baseUrl base URL of the application under test.
 */
public record UIConfig(
        String baseUrl
) {
}