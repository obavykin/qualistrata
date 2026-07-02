package com.bavykin.qualistrata.unix.interactive;

import java.time.Duration;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Validates interactive command scripts before transport execution.
 */
public class InteractiveCommandScriptValidator {

    /**
     * Validates an interactive command script.
     *
     * @param script script to validate.
     */
    public void validate(InteractiveCommandScript script) {
        if (script == null) {
            throw new InteractiveCommandException("Interactive command script must not be null.");
        }

        if (script.command() == null || script.command().isBlank()) {
            throw new InteractiveCommandException("Interactive command must not be blank.");
        }

        validateTimeout(script.timeout());

        if (script.promptResponses() == null) {
            throw new InteractiveCommandException("Interactive prompt responses must not be null.");
        }

        for (InteractivePromptResponse response : script.promptResponses()) {
            validatePromptResponse(response);
        }
    }

    private void validateTimeout(Duration timeout) {
        if (timeout == null) {
            throw new InteractiveCommandException("Interactive command timeout must not be null.");
        }

        if (timeout.isZero() || timeout.isNegative()) {
            throw new InteractiveCommandException("Interactive command timeout must be positive.");
        }
    }

    private void validatePromptResponse(InteractivePromptResponse response) {
        if (response == null) {
            throw new InteractiveCommandException("Interactive prompt response must not be null.");
        }

        if (response.promptPattern() == null || response.promptPattern().isBlank()) {
            throw new InteractiveCommandException("Interactive prompt pattern must not be blank.");
        }

        if (response.response() == null) {
            throw new InteractiveCommandException("Interactive prompt response value must not be null.");
        }

        try {
            Pattern.compile(response.promptPattern());
        } catch (PatternSyntaxException exception) {
            throw new InteractiveCommandException("Interactive prompt pattern is not a valid regular expression: " + response.promptPattern(), exception);
        }
    }
}
