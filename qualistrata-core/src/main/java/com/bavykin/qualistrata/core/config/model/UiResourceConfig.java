package com.bavykin.qualistrata.core.config.model;

/**
 * Generic web UI resource configuration.
 *
 * <p>This model knows only how to reach a browser-accessible UI. Product
 * concepts such as roles, pages, navigation flows, and login implementations
 * belong to consumer projects or UI module adapters.</p>
 *
 * @param type optional UI resource type, for example {@code web-ui}.
 * @param baseUrl base URL of the application under test.
 * @param defaultAccess default access profile alias used when caller does not
 *                      explicitly request one.
 */
public record UiResourceConfig(
        String type,
        String baseUrl,
        String defaultAccess
) {
}
