package com.bavykin.qualistrata.unix.ssh;

/**
 * Validates SSH connection requests.
 *
 * <p>The validator checks generic SSH request structure only. It does not
 * validate network reachability, authentication success, key file existence, or
 * server compatibility.</p>
 */
public class SshConnectionRequestValidator {

    /**
     * Creates an SSH connection request validator.
     */
    public SshConnectionRequestValidator() {
    }

    /**
     * Validates an SSH connection request.
     *
     * @param request request to validate.
     * @throws SshConnectionException if the request is structurally invalid.
     */
    public void validate(SshConnectionRequest request) {
        if (request == null) {
            throw new SshConnectionException("SSH connection request must not be null.");
        }

        requireNotBlank(request.host(), "SSH host must not be blank.");
        requirePositive(request.port(), "SSH port must be positive when configured.");
        requireNotBlank(request.username(), "SSH username must not be blank.");

        if (request.connectTimeout() != null && request.connectTimeout().isNegative()) {
            throw new SshConnectionException("SSH connection timeout must not be negative.");
        }

        if (request.connectTimeout() != null && request.connectTimeout().isZero()) {
            throw new SshConnectionException("SSH connection timeout must not be zero.");
        }
    }

    /**
     * Requires a string value to be non-null and non-blank.
     *
     * @param value value to check.
     * @param message exception message.
     */
    private void requireNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new SshConnectionException(message);
        }
    }

    /**
     * Requires an optional integer value to be positive when present.
     *
     * @param value value to check.
     * @param message exception message.
     */
    private void requirePositive(Integer value, String message) {
        if (value != null && value <= 0) {
            throw new SshConnectionException(message);
        }
    }
}