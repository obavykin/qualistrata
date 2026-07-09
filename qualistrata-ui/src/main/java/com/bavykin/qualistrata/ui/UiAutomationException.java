package com.bavykin.qualistrata.ui;

/**
 * Base runtime exception for generic UI automation infrastructure failures.
 *
 * <p>The exception belongs to {@code qualistrata-ui} and intentionally does
 * not carry any product-specific meaning. Consumer projects should wrap their
 * own business-level failures separately when required.</p>
 */
public class UiAutomationException extends RuntimeException {

    /**
     * Creates an exception with message.
     *
     * @param message diagnostic message.
     */
    public UiAutomationException(String message) {
        super(message);
    }

    /**
     * Creates an exception with message and cause.
     *
     * @param message diagnostic message.
     * @param cause original failure.
     */
    public UiAutomationException(String message, Throwable cause) {
        super(message, cause);
    }
}
