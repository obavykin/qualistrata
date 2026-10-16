package com.bavykin.qualistrata.ui.testng;

/**
 * Raised when UI mutation classification policy is violated.
 */
public class UiMutationValidationException extends RuntimeException {

    /**
     * Creates exception with message.
     *
     * @param message message value.
     */
    public UiMutationValidationException(String message) {
        super(message);
    }
}
