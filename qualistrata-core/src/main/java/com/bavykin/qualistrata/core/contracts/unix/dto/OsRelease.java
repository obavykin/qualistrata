package com.bavykin.qualistrata.core.contracts.unix.dto;

import java.util.Map;

/**
 * Parsed representation of Unix/Linux operating system release information.
 *
 * <p>The record is designed around the common {@code /etc/os-release} format,
 * but keeps the original key-value data in {@code raw} so that project-specific
 * validators can inspect distribution-specific fields without changing the
 * generic framework model.</p>
 *
 * @param id operating system identifier, usually the {@code ID} value.
 * @param name human-readable operating system name, usually the {@code NAME}
 *             value.
 * @param prettyName full display name, usually the {@code PRETTY_NAME} value.
 * @param version operating system version string, usually the {@code VERSION}
 *                value.
 * @param versionId machine-readable version identifier, usually the
 *                  {@code VERSION_ID} value.
 * @param raw original parsed release fields by key.
 */
public record OsRelease(
        String id,
        String name,
        String prettyName,
        String version,
        String versionId,
        Map<String, String> raw
) {
}