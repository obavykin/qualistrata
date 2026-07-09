package com.bavykin.qualistrata.unix.interactive;

/**
 * A response rule for an interactive command prompt.
 *
 * <p>The prompt is represented by a regular expression matched against the
 * accumulated command transcript. The response is sent to the command stdin
 * when the prompt is seen. Secret responses are never rendered by QualiStrata
 * itself and allow consumers to handle passwords without leaking them into
 * framework diagnostics.</p>
 *
 * @param promptPattern regular expression used to detect the prompt.
 * @param response response sent to stdin.
 * @param secret whether the response contains sensitive data.
 */
public record InteractivePromptResponse(
        String promptPattern,
        String response,
        boolean secret
) {

    /**
     * Creates a regular non-secret prompt response.
     *
     * @param promptPattern prompt regular expression.
     * @param response response text.
     * @return prompt response rule.
     */
    public static InteractivePromptResponse plain(String promptPattern, String response) {
        return new InteractivePromptResponse(promptPattern, response, false);
    }

    /**
     * Creates a secret prompt response.
     *
     * @param promptPattern prompt regular expression.
     * @param response secret response text.
     * @return secret prompt response rule.
     */
    public static InteractivePromptResponse secret(String promptPattern, String response) {
        return new InteractivePromptResponse(promptPattern, response, true);
    }
}
