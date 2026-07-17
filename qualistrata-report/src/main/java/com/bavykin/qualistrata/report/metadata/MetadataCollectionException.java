package com.bavykin.qualistrata.report.metadata;

/**
 * Indicates that test-run metadata could not be loaded or merged safely.
 */
public final class MetadataCollectionException extends RuntimeException {

    /**
     * Creates a metadata collection exception.
     *
     * @param message contextual failure message
     */
    public MetadataCollectionException(String message) {
        super(message);
    }

    /**
     * Creates a metadata collection exception with its root cause.
     *
     * @param message contextual failure message
     * @param cause root cause
     */
    public MetadataCollectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
