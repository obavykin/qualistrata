package com.bavykin.qualistrata.unix.file;

/**
 * Small guard for dangerous remote directory cleanup paths.
 *
 * <p>The guard is deliberately conservative but generic. It prevents the most
 * obvious accidental cleanup mistakes such as empty path, root path, current
 * directory, or parent directory.</p>
 */
final class RemoteFilePathGuard {

    void requireSafeDirectoryForCleanup(String directory) {
        if (directory == null || directory.isBlank()) {
            throw new RemoteFileException("Remote directory must not be blank.");
        }

        String normalized = directory.trim();

        if ("/".equals(normalized) || ".".equals(normalized) || "..".equals(normalized)) {
            throw new RemoteFileException("Unsafe remote cleanup directory: " + directory);
        }

        if (normalized.endsWith("/..") || normalized.contains("/../")) {
            throw new RemoteFileException("Unsafe remote cleanup directory containing parent traversal: " + directory);
        }
    }

    void requireGlobPattern(String globPattern) {
        if (globPattern == null || globPattern.isBlank()) {
            throw new RemoteFileException("Remote cleanup glob pattern must not be blank.");
        }
    }
}
