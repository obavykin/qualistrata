package com.bavykin.qualistrata.report.model;

import java.util.Objects;

/**
 * Stable framework-neutral identity of one concrete test invocation.
 *
 * <p>The adapter owns creation of {@code uniqueId}. The neutral model deliberately
 * does not derive identifiers from class and method names because parameterized,
 * dynamic, factory-created, and retried executions may share those values.</p>
 *
 * @param uniqueId stable adapter-supplied identifier for the concrete invocation
 * @param framework framework that produced the invocation
 * @param engineId native engine identifier, when available
 * @param suiteName logical suite or execution-context name
 * @param className fully qualified test class name
 * @param methodName test method name
 * @param invocationId adapter-supplied invocation identity, when applicable
 */
public record TestIdentifier(
        String uniqueId,
        TestFramework framework,
        String engineId,
        String suiteName,
        String className,
        String methodName,
        String invocationId
) {

    /**
     * Creates and validates a test identifier.
     */
    public TestIdentifier {
        uniqueId = requireUniqueId(uniqueId);
        Objects.requireNonNull(framework, "framework");
        engineId = normalize(engineId);
        suiteName = normalize(suiteName);
        className = normalize(className);
        methodName = normalize(methodName);
        invocationId = normalize(invocationId);
    }

    private static String requireUniqueId(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException("uniqueId must not be blank");
        }
        return normalized;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
