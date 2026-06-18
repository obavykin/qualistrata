package com.bavykin.qualistrata.unix.environment.component;

/**
 * Defines which command output stream should be used as the actual component
 * value for environment baseline matching.
 *
 * <p>Some tools print version information to standard output, while others,
 * such as Java, often print version information to standard error. The output
 * source allows a consuming project to choose the correct stream without making
 * the component check product-specific.</p>
 */
public enum UnixComponentOutputSource {

    /**
     * Use command standard output as the actual value.
     */
    STDOUT,

    /**
     * Use command standard error as the actual value.
     */
    STDERR,

    /**
     * Use standard output when it is not blank, otherwise fall back to standard
     * error.
     */
    STDOUT_THEN_STDERR,

    /**
     * Combine standard output and standard error into a single actual value.
     */
    COMBINED
}