package com.bavykin.qualistrata.unix.file;

import java.time.Instant;

/**
 * Metadata for a file or directory visible through a remote file client.
 *
 * @param path absolute or implementation-specific remote path.
 * @param directory whether the entry is a directory.
 * @param sizeBytes entry size in bytes.
 * @param modifiedAt last modification timestamp when available.
 */
public record RemoteFileInfo(
        String path,
        boolean directory,
        long sizeBytes,
        Instant modifiedAt
) {
}
