package com.bavykin.qualistrata.core.profile.model;

import java.util.Map;

/**
 * Expected third-party component baseline.
 *
 * <p>Describes how a component should be detected and which versionId is expected
 * for a particular release and host.
 *
 * <p>Example:
 *
 * <pre>
 * components:
 *   apache:
 *     enabled: true
 *     detector: apache
 *     versionPrefix: "2.4"
 *
 *   customTool:
 *     enabled: true
 *     detector: command
 *     command: "/opt/app/bin/custom-tool --versionId"
 *     versionPrefix: "6.5"
 * </pre>
 *
 * @param enabled whether this component should be validated
 * @param detector detector identifier, for example {@code apache}, {@code oracle-client}, {@code command}
 * @param version exact expected versionId
 * @param versionPrefix expected versionId prefix
 * @param command optional command for generic command-based detection
 * @param properties additional detector-specific parameters
 */
public record ComponentProfile(
        Boolean enabled,
        String detector,
        String version,
        String versionPrefix,
        String command,
        Map<String, Object> properties
) {
}