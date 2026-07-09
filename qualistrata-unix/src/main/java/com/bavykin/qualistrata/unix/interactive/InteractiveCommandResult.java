package com.bavykin.qualistrata.unix.interactive;

import java.time.Duration;
import java.util.List;

/**
 * Result of an interactive Unix command execution.
 *
 * <p>The transcript contains data produced by the command. QualiStrata does not
 * append configured responses to the transcript, which prevents framework code
 * from leaking secret responses such as passwords.</p>
 *
 * @param exitCode process exit code.
 * @param transcript command stdout/transcript accumulated by the transport.
 * @param stderr command stderr.
 * @param matchedPromptPatterns prompt patterns that were matched and answered.
 * @param duration execution duration.
 */
public record InteractiveCommandResult(
        int exitCode,
        String transcript,
        String stderr,
        List<String> matchedPromptPatterns,
        Duration duration
) {

    /**
     * Creates a result and defensively copies matched prompt patterns.
     */
    public InteractiveCommandResult {
        matchedPromptPatterns = matchedPromptPatterns == null
                ? List.of()
                : List.copyOf(matchedPromptPatterns);
    }
}
