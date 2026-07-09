package com.bavykin.qualistrata.unix.ssh.jsch.file;

import com.bavykin.qualistrata.unix.file.RemoteFileClient;
import com.bavykin.qualistrata.unix.file.RemoteFileException;
import com.bavykin.qualistrata.unix.file.RemoteFileInfo;
import com.bavykin.qualistrata.unix.ssh.SshConnectionException;
import com.bavykin.qualistrata.unix.ssh.SshConnectionRequest;
import com.bavykin.qualistrata.unix.ssh.SshConnectionRequestValidator;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * JSch/SFTP-backed implementation of {@link RemoteFileClient}.
 *
 * <p>The client owns one connected SSH session and opens short-lived SFTP or
 * exec channels for individual file operations. It is intentionally generic and
 * contains no TNT-specific dataset, archive, or batch-flow assumptions.</p>
 */
public class JschSftpRemoteFileClient implements RemoteFileClient, AutoCloseable {

    private static final int DEFAULT_SSH_PORT = 22;
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 30_000;
    private static final int DEFAULT_CHANNEL_CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final long POLL_INTERVAL_MILLIS = 50L;

    private final Session session;

    /**
     * Opens a JSch/SFTP remote file client from a generic SSH connection request.
     *
     * @param request SSH connection request.
     */
    public JschSftpRemoteFileClient(SshConnectionRequest request) {
        this(new JSch(), new SshConnectionRequestValidator(), request);
    }

    JschSftpRemoteFileClient(
            JSch jsch,
            SshConnectionRequestValidator requestValidator,
            SshConnectionRequest request
    ) {
        if (jsch == null) {
            throw new RemoteFileException("JSch instance must not be null.");
        }

        if (requestValidator == null) {
            throw new RemoteFileException("SSH connection request validator must not be null.");
        }

        requestValidator.validate(request);
        this.session = openSession(jsch, request);
    }

    @Override
    public void uploadFile(Path localFile, String remotePath) {
        if (localFile == null) {
            throw new RemoteFileException("Local file path must not be null.");
        }

        ChannelSftp channel = openSftpChannel();

        try {
            createParentDirectories(channel, remotePath);
            channel.put(localFile.toString(), remotePath, ChannelSftp.OVERWRITE);
        } catch (SftpException exception) {
            throw new RemoteFileException("Failed to upload file to remote path: " + remotePath, exception);
        } finally {
            channel.disconnect();
        }
    }

    @Override
    public boolean exists(String remotePath) {
        ChannelSftp channel = openSftpChannel();

        try {
            channel.lstat(remotePath);
            return true;
        } catch (SftpException exception) {
            return false;
        } finally {
            channel.disconnect();
        }
    }

    @Override
    public long size(String remotePath) {
        ChannelSftp channel = openSftpChannel();

        try {
            return channel.lstat(remotePath).getSize();
        } catch (SftpException exception) {
            throw new RemoteFileException("Failed to read remote file size: " + remotePath, exception);
        } finally {
            channel.disconnect();
        }
    }

    @Override
    public String checksumSha256(String remotePath) {
        String result = executeRemoteCommand("sha256sum -- " + shellQuote(remotePath));
        String trimmed = result.trim();

        if (trimmed.isEmpty()) {
            throw new RemoteFileException("Remote sha256sum returned empty output for: " + remotePath);
        }

        return trimmed.split("\\s+")[0];
    }

    @Override
    public List<RemoteFileInfo> list(String remoteDirectory) {
        ChannelSftp channel = openSftpChannel();

        try {
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> rawEntries = channel.ls(remoteDirectory);
            List<RemoteFileInfo> entries = new ArrayList<>();

            for (ChannelSftp.LsEntry entry : rawEntries) {
                if (".".equals(entry.getFilename()) || "..".equals(entry.getFilename())) {
                    continue;
                }

                String path = joinRemotePath(remoteDirectory, entry.getFilename());
                SftpATTRS attrs = entry.getAttrs();
                entries.add(new RemoteFileInfo(
                        path,
                        attrs.isDir(),
                        attrs.isDir() ? 0L : attrs.getSize(),
                        Instant.ofEpochSecond(attrs.getMTime())
                ));
            }

            return entries;
        } catch (SftpException exception) {
            throw new RemoteFileException("Failed to list remote directory: " + remoteDirectory, exception);
        } finally {
            channel.disconnect();
        }
    }

    @Override
    public int cleanDirectory(String remoteDirectory, String globPattern) {
        requireSafeDirectory(remoteDirectory);
        requireGlobPattern(globPattern);

        String pattern = joinRemotePath(remoteDirectory, globPattern);
        int deleted = 0;

        ChannelSftp channel = openSftpChannel();

        try {
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> entries = channel.ls(pattern);

            for (ChannelSftp.LsEntry entry : entries) {
                if (!entry.getAttrs().isDir()) {
                    channel.rm(joinRemotePath(remoteDirectory, entry.getFilename()));
                    deleted++;
                }
            }

            return deleted;
        } catch (SftpException exception) {
            if (exception.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return deleted;
            }

            throw new RemoteFileException("Failed to clean remote directory: " + remoteDirectory, exception);
        } finally {
            channel.disconnect();
        }
    }

    /**
     * Disconnects the owned SSH session.
     */
    @Override
    public void close() {
        session.disconnect();
    }

    private Session openSession(JSch jsch, SshConnectionRequest request) {
        try {
            if (request.keyPath() != null && !request.keyPath().isBlank()) {
                jsch.addIdentity(request.keyPath());
            }

            Session created = jsch.getSession(
                    request.username(),
                    request.host(),
                    request.port() == null ? DEFAULT_SSH_PORT : request.port()
            );

            if (request.password() != null && !request.password().isBlank()) {
                created.setPassword(request.password().getBytes(StandardCharsets.UTF_8));
            }

            created.setConfig("StrictHostKeyChecking", "no");
            created.connect(request.connectTimeout() == null
                    ? DEFAULT_CONNECT_TIMEOUT_MILLIS
                    : Math.toIntExact(request.connectTimeout().toMillis()));

            return created;
        } catch (JSchException exception) {
            throw new SshConnectionException("Failed to open SFTP session to "
                    + request.username() + "@" + request.host(), exception);
        }
    }

    private ChannelSftp openSftpChannel() {
        ensureOpen();

        try {
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(DEFAULT_CHANNEL_CONNECT_TIMEOUT_MILLIS);

            return channel;
        } catch (JSchException exception) {
            throw new RemoteFileException("Failed to open SFTP channel.", exception);
        }
    }

    private String executeRemoteCommand(String command) {
        ensureOpen();

        ChannelExec channel = null;

        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);

            InputStream stdoutStream = channel.getInputStream();
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();
            channel.setErrStream(stderr);
            channel.connect(DEFAULT_CHANNEL_CONNECT_TIMEOUT_MILLIS);

            while (!channel.isClosed()) {
                drainAvailableOutput(stdoutStream, stdout);
                sleepBeforeNextPoll();
            }

            drainAvailableOutput(stdoutStream, stdout);

            if (channel.getExitStatus() != 0) {
                throw new RemoteFileException("Remote command failed with exit code "
                        + channel.getExitStatus()
                        + ": "
                        + stderr.toString(StandardCharsets.UTF_8));
            }

            return stdout.toString(StandardCharsets.UTF_8);
        } catch (JSchException | IOException exception) {
            throw new RemoteFileException("Failed to execute remote file diagnostic command.", exception);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    private void createParentDirectories(ChannelSftp channel, String remotePath) {
        int separator = remotePath.lastIndexOf('/');

        if (separator <= 0) {
            return;
        }

        String parent = remotePath.substring(0, separator);
        String[] parts = parent.split("/");
        String current = parent.startsWith("/") ? "/" : "";

        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }

            current = "/".equals(current) ? current + part : current + "/" + part;

            try {
                channel.stat(current);
            } catch (SftpException exception) {
                try {
                    channel.mkdir(current);
                } catch (SftpException mkdirException) {
                    throw new RemoteFileException("Failed to create remote directory: " + current, mkdirException);
                }
            }
        }
    }

    private void drainAvailableOutput(InputStream stream, ByteArrayOutputStream output) throws IOException {
        byte[] buffer = new byte[8192];

        while (stream.available() > 0) {
            int read = stream.read(buffer, 0, Math.min(buffer.length, stream.available()));

            if (read < 0) {
                return;
            }

            output.write(buffer, 0, read);
        }
    }

    private void sleepBeforeNextPoll() {
        try {
            Thread.sleep(POLL_INTERVAL_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RemoteFileException("Remote file command execution was interrupted.", exception);
        }
    }

    private String joinRemotePath(String directory, String fileName) {
        if (directory.endsWith("/")) {
            return directory + fileName;
        }

        return directory + "/" + fileName;
    }

    private String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }

    private void requireSafeDirectory(String directory) {
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

    private void requireGlobPattern(String globPattern) {
        if (globPattern == null || globPattern.isBlank()) {
            throw new RemoteFileException("Remote cleanup glob pattern must not be blank.");
        }
    }

    private void ensureOpen() {
        if (!session.isConnected()) {
            throw new RemoteFileException("SFTP session is not open.");
        }
    }
}
