package com.bavykin.qualistrata.unix.command;

import com.bavykin.qualistrata.unix.session.UnixSessionException;

/**
 * Builds POSIX-compatible Unix commands with optional shell profile sourcing.
 *
 * <p>Enterprise Unix environments often require sourcing a profile before
 * tools such as Python, Apache, database clients, or custom project utilities
 * become available in non-interactive SSH sessions.</p>
 *
 * <p>When a shell profile is provided, the profile is sourced in the same shell
 * invocation as the actual command. Profile stdout and stderr are redirected to
 * {@code /dev/null} so banner/debug output from the profile does not pollute
 * command output that may be parsed by detectors.</p>
 */
public class UnixShellProfileCommandBuilder {

    /**
     * Creates a shell profile command builder.
     */
    public UnixShellProfileCommandBuilder() {
    }

    /**
     * Wraps a command with profile sourcing when a profile is provided.
     *
     * @param command command to execute.
     * @param shellProfile optional shell profile path.
     * @return command with optional profile sourcing.
     */
    public String build(String command, String shellProfile) {
        if (command == null || command.isBlank()) {
            throw new UnixSessionException("Unix command must not be blank.");
        }

        if (shellProfile == null || shellProfile.isBlank()) {
            return command.trim();
        }

        return ". "
                + singleQuote(shellProfile.trim())
                + " >/dev/null 2>&1 && "
                + command.trim();
    }

    /**
     * Single-quotes a shell value using POSIX-compatible escaping.
     *
     * @param value raw shell value.
     * @return safely quoted shell value.
     */
    private String singleQuote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }
}