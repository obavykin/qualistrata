package com.bavykin.qualistrata.ui.testng;

/**
 * Raised when UI mutation classification policy is violated.
 */
public class UiMutationValidationException extends RuntimeException {

    /** Creates exception with message. */
    public UiMutationValidationException(String message) {
        super(message);
    }
}
