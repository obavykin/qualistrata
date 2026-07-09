package com.bavykin.qualistrata.unix.file;

import com.bavykin.qualistrata.core.telemetry.TelemetryEvent;
import com.bavykin.qualistrata.core.telemetry.TelemetryEventType;
import com.bavykin.qualistrata.core.telemetry.TelemetrySink;

import java.nio.file.Path;
import java.util.List;

/**
 * Telemetry-decorating {@link RemoteFileClient}.
 *
 * <p>The decorator records generic remote file operation timings without
 * changing file operation semantics. It is deliberately generic and does not
 * know anything about TNT batch flows, datasets, or business utility names.
 * Consumer layers can add their own business-level metrics around these
 * primitives.</p>
 */
public class MeasuredRemoteFileClient implements RemoteFileClient {

    /** Metric emitted for file uploads. */
    public static final String FILE_UPLOAD_METRIC = "unix.file.upload.ms";

    /** Metric emitted for remote directory listing. */
    public static final String DIRECTORY_LIST_METRIC = "unix.directory.list.ms";

    /** Metric emitted for remote file checksum calculation. */
    public static final String FILE_CHECKSUM_METRIC = "unix.file.checksum.ms";

    /** Metric emitted for remote directory cleanup. */
    public static final String DIRECTORY_CLEAN_METRIC = "unix.directory.clean.ms";

    private final RemoteFileClient delegate;
    private final TelemetrySink telemetry;

    /**
     * Creates measured file client.
     *
     * @param delegate file client to decorate.
     * @param telemetry telemetry sink.
     */
    public MeasuredRemoteFileClient(
            RemoteFileClient delegate,
            TelemetrySink telemetry
    ) {
        if (delegate == null) {
            throw new RemoteFileException("Measured remote file delegate must not be null.");
        }

        if (telemetry == null) {
            throw new RemoteFileException("Telemetry sink must not be null.");
        }

        this.delegate = delegate;
        this.telemetry = telemetry;
    }

    @Override
    public void uploadFile(Path localFile, String remotePath) {
        long started = System.nanoTime();

        try {
            delegate.uploadFile(localFile, remotePath);
            record(FILE_UPLOAD_METRIC, started, "success")
                    .field("remotePath", remotePath)
                    .buildAndRecord(telemetry);
        } catch (RuntimeException exception) {
            record(FILE_UPLOAD_METRIC, started, "failed")
                    .field("remotePath", remotePath)
                    .field("exceptionType", exception.getClass().getName())
                    .buildAndRecord(telemetry);

            throw exception;
        }
    }

    @Override
    public boolean exists(String remotePath) {
        return delegate.exists(remotePath);
    }

    @Override
    public long size(String remotePath) {
        return delegate.size(remotePath);
    }

    @Override
    public String checksumSha256(String remotePath) {
        long started = System.nanoTime();

        try {
            String checksum = delegate.checksumSha256(remotePath);
            record(FILE_CHECKSUM_METRIC, started, "success")
                    .field("remotePath", remotePath)
                    .buildAndRecord(telemetry);

            return checksum;
        } catch (RuntimeException exception) {
            record(FILE_CHECKSUM_METRIC, started, "failed")
                    .field("remotePath", remotePath)
                    .field("exceptionType", exception.getClass().getName())
                    .buildAndRecord(telemetry);

            throw exception;
        }
    }

    @Override
    public List<RemoteFileInfo> list(String remoteDirectory) {
        long started = System.nanoTime();

        try {
            List<RemoteFileInfo> entries = delegate.list(remoteDirectory);
            record(DIRECTORY_LIST_METRIC, started, "success")
                    .field("remoteDirectory", remoteDirectory)
                    .field("entryCount", entries.size())
                    .buildAndRecord(telemetry);

            return entries;
        } catch (RuntimeException exception) {
            record(DIRECTORY_LIST_METRIC, started, "failed")
                    .field("remoteDirectory", remoteDirectory)
                    .field("exceptionType", exception.getClass().getName())
                    .buildAndRecord(telemetry);

            throw exception;
        }
    }

    @Override
    public int cleanDirectory(String remoteDirectory, String globPattern) {
        long started = System.nanoTime();

        try {
            int deleted = delegate.cleanDirectory(remoteDirectory, globPattern);
            record(DIRECTORY_CLEAN_METRIC, started, "success")
                    .field("remoteDirectory", remoteDirectory)
                    .field("globPattern", globPattern)
                    .field("deletedCount", deleted)
                    .buildAndRecord(telemetry);

            return deleted;
        } catch (RuntimeException exception) {
            record(DIRECTORY_CLEAN_METRIC, started, "failed")
                    .field("remoteDirectory", remoteDirectory)
                    .field("globPattern", globPattern)
                    .field("exceptionType", exception.getClass().getName())
                    .buildAndRecord(telemetry);

            throw exception;
        }
    }

    private EventRecorder record(String name, long started, String status) {
        long durationNanos = System.nanoTime() - started;

        return new EventRecorder(TelemetryEvent.builder(name, TelemetryEventType.DURATION)
                .tag("module", "unix")
                .tag("operation", "file")
                .tag("status", status)
                .field("durationNanos", durationNanos)
                .field("durationMillis", durationNanos / 1_000_000.0));
    }

    /** Small fluent helper that keeps recording call sites compact. */
    private record EventRecorder(TelemetryEvent.Builder builder) {

        EventRecorder field(String key, Object value) {
            builder.field(key, value);
            return this;
        }

        void buildAndRecord(TelemetrySink sink) {
            sink.record(builder.build());
        }
    }
}
