package com.bavykin.qualistrata.core.config.secrets;

/**
 * Wrapper for sensitive string values.
 *
 * <p>The wrapper makes accidental logging safer because {@link #toString()}
 * never exposes the underlying value. Call {@link #value()} only at the
 * boundary where the secret is passed to a client library.</p>
 */
public final class SecretValue {

    private final String value;

    /**
     * Creates a secret value wrapper.
     *
     * @param value raw secret value.
     */
    public SecretValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Secret value must not be blank.");
        }
        this.value = value;
    }

    /**
     * Returns the raw secret value for client-library integration only.
     *
     * @return raw secret value.
     */
    public String value() {
        return value;
    }

    /**
     * Masks the secret for logs, reports, and diagnostics.
     *
     * @return masked value.
     */
    @Override
    public String toString() {
        return "***";
    }
}
