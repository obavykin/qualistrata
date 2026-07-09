package com.bavykin.qualistrata.core.exceptions;

/**
 * Unchecked exception used for framework-level assertion failures.
 *
 * <p>This class should be used when QualiStrata detects that an expected
 * technical condition was not met. It should not be used to hide product
 * defects or to retry failed business validations.</p>
 */
public class AssertionException extends RuntimeException {

    /**
     * Creates an assertion exception with a descriptive message.
     *
     * @param message explanation of the failed assertion.
     */
    public AssertionException(String message) {
        super(message);
    }

    /**
     * Creates an assertion exception with a descriptive message and root cause.
     *
     * @param message explanation of the failed assertion.
     * @param cause original exception that caused this failure.
     */
    public AssertionException(String message, Throwable cause) {
        super(message, cause);
    }
}