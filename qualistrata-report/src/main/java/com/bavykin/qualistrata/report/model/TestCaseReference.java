package com.bavykin.qualistrata.report.model;

/**
 * External or manual test-case reference associated with an automated test.
 *
 * @param id external test-case identifier
 * @param system source system, when available
 */
public record TestCaseReference(String id, String system) {

    /**
     * Creates and normalizes an external test-case reference.
     */
    public TestCaseReference {
        id = requireText(id, "id");
        system = normalize(system);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
