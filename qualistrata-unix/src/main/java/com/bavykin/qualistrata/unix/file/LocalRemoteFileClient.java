package com.bavykin.qualistrata.unix.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * Local filesystem implementation of {@link RemoteFileClient}.
 *
 * <p>The implementation is useful for unit tests and for local diagnostics. It
 * follows the same contract as SFTP-backed implementations while avoiding any
 * external SSH infrastructure.</p>
 */
public class LocalRemoteFileClient implements RemoteFileClient {

    private final RemoteFilePathGuard pathGuard;

    /**
     * Creates local remote-file adapter.
     */
    public LocalRemoteFileClient() {
        this(new RemoteFilePathGuard());
    }

    LocalRemoteFileClient(RemoteFilePathGuard pathGuard) {
        this.pathGuard = pathGuard;
    }

    @Override
    public void uploadFile(Path localFile, String remotePath) {
        if (localFile == null) {
            throw new RemoteFileException("Local file path must not be null.");
        }

        Path destination = Path.of(remotePath);

        try {
            if (destination.getParent() != null) {
                Files.createDirectories(destination.getParent());
            }

            Files.copy(localFile, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new RemoteFileException("Failed to upload local file to: " + remotePath, exception);
        }
    }

    @Override
    public boolean exists(String remotePath) {
        return Files.exists(Path.of(remotePath));
    }

    @Override
    public long size(String remotePath) {
        try {
            return Files.size(Path.of(remotePath));
        } catch (IOException exception) {
            throw new RemoteFileException("Failed to read file size: " + remotePath, exception);
        }
    }

    @Override
    public String checksumSha256(String remotePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (InputStream input = Files.newInputStream(Path.of(remotePath));
                 DigestInputStream digestInput = new DigestInputStream(input, digest)) {
                digestInput.transferTo(OutputStreamDiscard.INSTANCE);
            }

            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new RemoteFileException("Failed to calculate SHA-256 checksum: " + remotePath, exception);
        }
    }

    @Override
    public List<RemoteFileInfo> list(String remoteDirectory) {
        Path directory = Path.of(remoteDirectory);
        List<RemoteFileInfo> entries = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                entries.add(new RemoteFileInfo(
                        entry.toString(),
                        Files.isDirectory(entry),
                        Files.isDirectory(entry) ? 0L : Files.size(entry),
                        Files.getLastModifiedTime(entry).toInstant()
                ));
            }

            return entries;
        } catch (IOException exception) {
            throw new RemoteFileException("Failed to list remote directory: " + remoteDirectory, exception);
        }
    }

    @Override
    public int cleanDirectory(String remoteDirectory, String globPattern) {
        pathGuard.requireSafeDirectoryForCleanup(remoteDirectory);
        pathGuard.requireGlobPattern(globPattern);

        int deleted = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(remoteDirectory), globPattern)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry)) {
                    Files.deleteIfExists(entry);
                    deleted++;
                }
            }

            return deleted;
        } catch (IOException exception) {
            throw new RemoteFileException("Failed to clean remote directory: " + remoteDirectory, exception);
        }
    }

    /** Output stream that discards checksum input bytes. */
    private static final class OutputStreamDiscard extends java.io.OutputStream {

        static final OutputStreamDiscard INSTANCE = new OutputStreamDiscard();

        @Override
        public void write(int value) {
            // Intentionally discards the value.
        }
    }
}
