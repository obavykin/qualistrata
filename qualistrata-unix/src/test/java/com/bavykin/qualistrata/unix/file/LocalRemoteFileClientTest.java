package com.bavykin.qualistrata.unix.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocalRemoteFileClientTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldUploadFileAndExposeMetadata() throws Exception {
        Path localFile = tempDir.resolve("source.txt");
        Files.writeString(localFile, "hello\n");

        Path remoteFile = tempDir.resolve("remote").resolve("target.txt");
        LocalRemoteFileClient client = new LocalRemoteFileClient();

        client.uploadFile(localFile, remoteFile.toString());

        assertTrue(client.exists(remoteFile.toString()));
        assertEquals(6L, client.size(remoteFile.toString()));
        assertEquals("5891b5b522d5df086d0ff0b110fbd9d21bb4fc7163af34d08286a2e846f6be03", client.checksumSha256(remoteFile.toString()));
    }

    @Test
    void shouldListDirectoryAndCheckEmptyState() throws Exception {
        Path directory = Files.createDirectory(tempDir.resolve("dir"));
        Path file = directory.resolve("file.dat");
        Files.writeString(file, "data");

        LocalRemoteFileClient client = new LocalRemoteFileClient();

        List<RemoteFileInfo> entries = client.list(directory.toString());

        assertFalse(client.isDirectoryEmpty(directory.toString()));
        assertEquals(1, entries.size());
        assertEquals(file.toString(), entries.getFirst().path());
        assertFalse(entries.getFirst().directory());
        assertEquals(4L, entries.getFirst().sizeBytes());
        assertNotNull(entries.getFirst().modifiedAt());
    }

    @Test
    void shouldCleanOnlyMatchingFiles() throws Exception {
        Path directory = Files.createDirectory(tempDir.resolve("clean"));
        Files.writeString(directory.resolve("qs_test_a.dat"), "a");
        Files.writeString(directory.resolve("qs_test_b.dat"), "b");
        Files.writeString(directory.resolve("keep.dat"), "c");

        LocalRemoteFileClient client = new LocalRemoteFileClient();

        int deleted = client.cleanDirectory(directory.toString(), "qs_test_*");

        assertEquals(2, deleted);
        assertFalse(Files.exists(directory.resolve("qs_test_a.dat")));
        assertFalse(Files.exists(directory.resolve("qs_test_b.dat")));
        assertTrue(Files.exists(directory.resolve("keep.dat")));
    }

    @Test
    void shouldRejectUnsafeCleanupDirectory() {
        LocalRemoteFileClient client = new LocalRemoteFileClient();

        assertThrows(RemoteFileException.class, () -> client.cleanDirectory("/", "*"));
        assertThrows(RemoteFileException.class, () -> client.cleanDirectory(".", "*"));
        assertThrows(RemoteFileException.class, () -> client.cleanDirectory("/tmp/../data", "*"));
    }
}
