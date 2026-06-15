package com.bavykin.qualistrata.unix.command;

/**
 * Validates Unix command execution requests.
 *
 * <p>The validator contains only generic request-level checks. It does not
 * validate host connectivity, credentials, shell availability, or command
 * semantics.</p>
 */
public class UnixCommandRequestValidator {

    /**
     * Creates a Unix command request validator.
     *
     * <p>The validator is stateless and can be reused by different Unix command
     * client implementations.</p>
     */
    public UnixCommandRequestValidator() {
    }

    /**
     * Validates a Unix command request.
     *
     * @param request request to validate.
     * @throws UnixCommandException if the request is invalid.
     */
    public void validate(UnixCommandRequest request) {
        if (request == null) {
            throw new UnixCommandException("Unix command request must not be null.");
        }

        if (request.command() == null || request.command().trim().isEmpty()) {
            throw new UnixCommandException("Unix command must not be blank.");
        }

        if (request.timeout() != null && request.timeout().isNegative()) {
            throw new UnixCommandException("Unix command timeout must not be negative.");
        }

        if (request.timeout() != null && request.timeout().isZero()) {
            throw new UnixCommandException("Unix command timeout must not be zero.");
        }
    }
}