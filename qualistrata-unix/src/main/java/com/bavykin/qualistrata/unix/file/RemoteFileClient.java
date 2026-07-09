package com.bavykin.qualistrata.unix.file;

import java.nio.file.Path;
import java.util.List;

/**
 * Generic remote file operations used by higher-level Unix and batch-flow
 * support code.
 *
 * <p>The contract intentionally does not expose the underlying transport. An
 * implementation may be SFTP-backed for real hosts, local-filesystem-backed for
 * tests, or replaced later by another transfer mechanism.</p>
 */
public interface RemoteFileClient {

    /**
     * Uploads a local file to a remote path.
     *
     * @param localFile local file to upload.
     * @param remotePath destination remote path including file name.
     */
    void uploadFile(Path localFile, String remotePath);

    /**
     * Checks whether a remote path exists.
     *
     * @param remotePath remote path.
     * @return {@code true} when the path exists.
     */
    boolean exists(String remotePath);

    /**
     * Returns remote file size in bytes.
     *
     * @param remotePath remote file path.
     * @return file size.
     */
    long size(String remotePath);

    /**
     * Calculates SHA-256 checksum of a remote file.
     *
     * @param remotePath remote file path.
     * @return lowercase hexadecimal SHA-256 checksum.
     */
    String checksumSha256(String remotePath);

    /**
     * Lists entries in a remote directory.
     *
     * @param remoteDirectory remote directory path.
     * @return directory entries excluding {@code .} and {@code ..}.
     */
    List<RemoteFileInfo> list(String remoteDirectory);

    /**
     * Deletes files matching a glob pattern from a remote directory.
     *
     * <p>Implementations must reject blank, root, or obviously unsafe directory
     * paths. This is a generic safety guard; consumer projects may still decide
     * how aggressively they clean non-production test directories.</p>
     *
     * @param remoteDirectory remote directory to clean.
     * @param globPattern file glob pattern, for example {@code *} or
     *                    {@code qs_test_*}.
     * @return number of deleted files.
     */
    int cleanDirectory(String remoteDirectory, String globPattern);

    /**
     * Checks whether a remote directory contains no entries.
     *
     * @param remoteDirectory remote directory path.
     * @return {@code true} when no entries are present.
     */
    default boolean isDirectoryEmpty(String remoteDirectory) {
        return list(remoteDirectory).isEmpty();
    }
}
