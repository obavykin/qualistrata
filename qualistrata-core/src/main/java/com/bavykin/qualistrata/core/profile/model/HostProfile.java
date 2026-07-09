package com.bavykin.qualistrata.core.profile.model;

/**
 * Release baseline for a single logical host.
 *
 * <p>A logical host is identified by an alias from the target configuration,
 * for example {@code app}, {@code db}, {@code mq}, or {@code reporting}.
 *
 * <p>This profile does not contain physical connection details such as hostname,
 * SSH port, or credentials. Those belong to {@code TargetConfig}.
 *
 * @param unix Unix-specific baseline for this host
 */
public record HostProfile(

        UnixProfile unix

) {
}