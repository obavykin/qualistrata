package com.bavykin.qualistrata.ui.junit;

/** Thrown when JUnit UI mutation classification violates configured policy. */
public class UiJunitMutationValidationException extends RuntimeException {

    /** Member. */
    /**
     * Creates a validation exception.
     *
     * @param message validation failure message.
     */
    public UiJunitMutationValidationException(String message) {
        super(message);
    }
}
