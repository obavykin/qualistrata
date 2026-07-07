package com.bavykin.qualistrata.ui.driver;

import java.nio.file.Path;

/**
 * Generic browser download descriptor.
 *
 * @param suggestedFileName browser-provided file name.
 * @param path downloaded file path when available.
 */
public record UiDownload(String suggestedFileName, Path path) {
}
