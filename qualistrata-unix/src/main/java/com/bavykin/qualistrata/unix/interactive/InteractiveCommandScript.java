package com.bavykin.qualistrata.unix.interactive;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Declarative script for an interactive Unix command.
 *
 * <p>The script contains the command line, expected prompts, responses to send
 * to stdin, timeout, and whether a pseudo terminal should be requested. It is a
 * generic QualiStrata concept and intentionally contains no consumer-project
 * details such as TNT utility names or password aliases.</p>
 *
 * @param command command line to execute.
 * @param promptResponses ordered prompt response rules.
 * @param timeout maximum duration of interactive execution.
 * @param allocatePty whether to request a pseudo terminal.
 */
public record InteractiveCommandScript(
        String command,
        List<InteractivePromptResponse> promptResponses,
        Duration timeout,
        boolean allocatePty
) {

    /**
     * Starts an interactive command script builder.
     *
     * @param command command line to execute.
     * @return script builder.
     */
    public static Builder command(String command) {
        return new Builder(command);
    }

    /**
     * Builder for interactive command scripts.
     */
    public static final class Builder {

        private final String command;
        private final List<InteractivePromptResponse> promptResponses = new ArrayList<>();
        private Duration timeout = Duration.ofMinutes(5);
        private boolean allocatePty = true;

        private Builder(String command) {
            this.command = command;
        }

        /**
         * Adds a non-secret response to a prompt.
         *
         * @param promptPattern prompt regular expression.
         * @param response response text.
         * @return this builder.
         */
        public Builder respondTo(String promptPattern, String response) {
            promptResponses.add(InteractivePromptResponse.plain(promptPattern, response));
            return this;
        }

        /**
         * Adds a secret response to a prompt.
         *
         * @param promptPattern prompt regular expression.
         * @param response secret response text.
         * @return this builder.
         */
        public Builder respondSecretTo(String promptPattern, String response) {
            promptResponses.add(InteractivePromptResponse.secret(promptPattern, response));
            return this;
        }

        /**
         * Configures command timeout.
         *
         * @param timeout maximum execution duration.
         * @return this builder.
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Configures whether the command should allocate a pseudo terminal.
         *
         * @param allocatePty whether to allocate PTY.
         * @return this builder.
         */
        public Builder allocatePty(boolean allocatePty) {
            this.allocatePty = allocatePty;
            return this;
        }

        /**
         * Builds the script.
         *
         * @return interactive command script.
         */
        public InteractiveCommandScript build() {
            return new InteractiveCommandScript(
                    command,
                    List.copyOf(promptResponses),
                    timeout,
                    allocatePty
            );
        }
    }
}
