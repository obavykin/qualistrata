package com.bavykin.qualistrata.unix.environment.detector;

/**
 * Builds POSIX-compatible Unix commands with optional shell profile sourcing.
 *
 * <p>Enterprise Unix environments often require sourcing a profile before
 * tools such as Python, Apache, or database clients become available in the
 * shell. This builder keeps that behavior generic and independent from any
 * consuming project.</p>
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
            throw new UnixComponentDetectorException("Unix detector command must not be blank.");
        }

        if (shellProfile == null || shellProfile.isBlank()) {
            return command.trim();
        }

        return ". "
                + singleQuote(shellProfile.trim())
                + " && "
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