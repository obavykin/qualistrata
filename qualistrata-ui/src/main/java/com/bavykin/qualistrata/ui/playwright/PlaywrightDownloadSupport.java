package com.bavykin.qualistrata.ui.playwright;

import com.bavykin.qualistrata.ui.driver.DownloadSupport;
import com.bavykin.qualistrata.ui.driver.UiDownload;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Playwright download helper.
 */
final class PlaywrightDownloadSupport implements DownloadSupport {

    private final Page page;
    private final Duration defaultTimeout;
    private static final Duration RESOURCE_RETRY_INTERVAL = Duration.ofMillis(100);
    private static final int RESOURCE_NOT_READY_STATUS = 404;
    private static final Pattern CONTENT_DISPOSITION_FILENAME = Pattern.compile(
            "(?i)(?:^|;)\\s*filename\\s*=\\s*\"?([^;\"]+)\"?"
    );
    private static final Pattern CONTENT_DISPOSITION_FILENAME_STAR = Pattern.compile(
            "(?i)(?:^|;)\\s*filename\\*\\s*=\\s*(?:UTF-8'')?([^;]+)"
    );

    PlaywrightDownloadSupport(Page page, Duration defaultTimeout) {
        this.page = page;
        this.defaultTimeout = defaultTimeout;
    }

    @Override
    public UiDownload waitForDownload(Runnable action) {
        return waitForDownload(action, defaultTimeout);
    }

    @Override
    public UiDownload waitForDownload(Runnable action, Duration timeout) {
        Page.WaitForDownloadOptions options = new Page.WaitForDownloadOptions()
                .setTimeout(timeout.toMillis());
        Download download = page.waitForDownload(options, action);
        return new UiDownload(download.suggestedFilename(), download.path());
    }

    @Override
    public UiDownload downloadFromUrl(String url) {
        Objects.requireNonNull(url, "Download URL must not be null.");
        if (url.isBlank()) {
            throw new IllegalArgumentException("Download URL must not be blank.");
        }

        URI resolvedUri = URI.create(page.url()).resolve(url);
        APIResponse response = awaitDownloadResource(resolvedUri);

        String suggestedFileName = resolveFileName(
                resolvedUri,
                response.headers()
        );

        try {
            Path directory = Files.createTempDirectory("qualistrata-download-");
            Path target = directory.resolve(suggestedFileName);
            Files.write(target, response.body());
            return new UiDownload(suggestedFileName, target);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to persist downloaded resource from '" + resolvedUri + "'.",
                    exception
            );
        } finally {
            response.dispose();
        }
    }

    /**
     * Polls a generated resource until it becomes available or the configured
     * download timeout expires.
     *
     * <p>TNT-style report exports can publish the target URL before the backing
     * file is visible to the HTTP endpoint. HTTP 404 is therefore treated as a
     * transient not-ready state; all other unsuccessful statuses fail
     * immediately.</p>
     */
    private APIResponse awaitDownloadResource(URI resolvedUri) {
        long deadline = System.nanoTime() + defaultTimeout.toNanos();
        int lastStatus = RESOURCE_NOT_READY_STATUS;

        while (true) {
            APIResponse response = page.context().request().get(resolvedUri.toString());
            if (response.ok()) {
                return response;
            }

            lastStatus = response.status();
            response.dispose();

            if (lastStatus != RESOURCE_NOT_READY_STATUS) {
                throw downloadFailure(resolvedUri, lastStatus);
            }
            if (System.nanoTime() >= deadline) {
                throw new IllegalStateException(
                        "Timed out waiting for download resource '" + resolvedUri
                                + "' to become available. Last HTTP status: "
                                + lastStatus + "."
                );
            }

            pauseBeforeResourceRetry(resolvedUri);
        }
    }

    private static void pauseBeforeResourceRetry(URI resolvedUri) {
        try {
            Thread.sleep(RESOURCE_RETRY_INTERVAL.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting for download resource '"
                            + resolvedUri + "' to become available.",
                    interrupted
            );
        }
    }

    private static IllegalStateException downloadFailure(URI resolvedUri, int status) {
        return new IllegalStateException(
                "Unable to download resource from '" + resolvedUri
                        + "'. HTTP status: " + status + "."
        );
    }

    private static String resolveFileName(URI uri, Map<String, String> headers) {
        String contentDisposition = contentDisposition(headers);
        String fromHeader = fileNameFromContentDisposition(contentDisposition);
        if (fromHeader != null && !fromHeader.isBlank()) {
            return sanitizeFileName(fromHeader);
        }

        String path = uri.getPath();
        if (path != null && !path.isBlank() && !path.endsWith("/")) {
            String fromPath = path.substring(path.lastIndexOf('/') + 1);
            if (!fromPath.isBlank()) {
                return sanitizeFileName(URLDecoder.decode(fromPath, StandardCharsets.UTF_8));
            }
        }
        return "download.bin";
    }

    private static String fileNameFromContentDisposition(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        Matcher encoded = CONTENT_DISPOSITION_FILENAME_STAR.matcher(value);
        if (encoded.find()) {
            return URLDecoder.decode(encoded.group(1).trim(), StandardCharsets.UTF_8);
        }
        Matcher plain = CONTENT_DISPOSITION_FILENAME.matcher(value);
        return plain.find() ? plain.group(1).trim() : null;
    }

    private static String contentDisposition(Map<String, String> headers) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase(Locale.ROOT)
                        .equals("content-disposition"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private static String sanitizeFileName(String value) {
        String sanitized = value.replace('\\', '_').replace('/', '_').trim();
        return sanitized.isBlank() ? "download.bin" : sanitized;
    }
}
