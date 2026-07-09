package com.bavykin.qualistrata.unix.file;

import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;
import com.bavykin.qualistrata.core.telemetry.TelemetrySink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MeasuredRemoteFileClientTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldRecordUploadAndListTelemetry() throws Exception {
        Path localFile = tempDir.resolve("source.dat");
        Files.writeString(localFile, "data");
        Path remoteDirectory = tempDir.resolve("remote");
        Path remoteFile = remoteDirectory.resolve("source.dat");
        RecordingSink sink = new RecordingSink();

        MeasuredRemoteFileClient client = new MeasuredRemoteFileClient(new LocalRemoteFileClient(), sink);
        client.uploadFile(localFile, remoteFile.toString());
        List<RemoteFileInfo> entries = client.list(remoteDirectory.toString());

        assertEquals(1, entries.size());
        assertEquals(2, sink.events().size());

        TelemetryEvent upload = sink.events().getFirst();
        assertEquals(MeasuredRemoteFileClient.FILE_UPLOAD_METRIC, upload.name());
        assertEquals("unix", upload.tags().get("module"));
        assertEquals("file", upload.tags().get("operation"));
        assertEquals("success", upload.tags().get("status"));
        assertEquals(remoteFile.toString(), upload.fields().get("remotePath"));
        assertTrue(upload.fields().containsKey("durationMillis"));

        TelemetryEvent list = sink.events().get(1);
        assertEquals(MeasuredRemoteFileClient.DIRECTORY_LIST_METRIC, list.name());
        assertEquals(1, list.fields().get("entryCount"));
    }

    @Test
    void shouldRecordChecksumAndCleanupTelemetry() throws Exception {
        Path directory = Files.createDirectory(tempDir.resolve("clean"));
        Path file = directory.resolve("qs_test_a.dat");
        Files.writeString(file, "data");
        RecordingSink sink = new RecordingSink();

        MeasuredRemoteFileClient client = new MeasuredRemoteFileClient(new LocalRemoteFileClient(), sink);
        String checksum = client.checksumSha256(file.toString());
        int deleted = client.cleanDirectory(directory.toString(), "qs_test_*");

        assertEquals("3a6eb0790f39ac87c94f3856b2dd2c5d110e6811602261a9a923d3bb23adc8b7", checksum);
        assertEquals(1, deleted);
        assertEquals(MeasuredRemoteFileClient.FILE_CHECKSUM_METRIC, sink.events().get(0).name());
        assertEquals(MeasuredRemoteFileClient.DIRECTORY_CLEAN_METRIC, sink.events().get(1).name());
        assertEquals(1, sink.events().get(1).fields().get("deletedCount"));
    }

    @Test
    void shouldRecordFailureAndRethrow() {
        RecordingSink sink = new RecordingSink();
        RemoteFileClient delegate = new LocalRemoteFileClient();
        MeasuredRemoteFileClient client = new MeasuredRemoteFileClient(delegate, sink);

        assertThrows(RemoteFileException.class, () -> client.cleanDirectory("/", "*"));

        TelemetryEvent event = sink.events().getFirst();
        assertEquals(MeasuredRemoteFileClient.DIRECTORY_CLEAN_METRIC, event.name());
        assertEquals("failed", event.tags().get("status"));
        assertEquals(RemoteFileException.class.getName(), event.fields().get("exceptionType"));
    }

    private static final class RecordingSink implements TelemetrySink {

        private final List<TelemetryEvent> events = new ArrayList<>();

        @Override
        public void record(TelemetryEvent event) {
            events.add(event);
        }

        List<TelemetryEvent> events() {
            return events;
        }
    }
}
