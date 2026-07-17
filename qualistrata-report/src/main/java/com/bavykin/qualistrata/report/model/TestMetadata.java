package com.bavykin.qualistrata.report.model;

import java.util.List;
import java.util.Objects;

/**
 * Optional framework-neutral metadata enriching one automated test.
 *
 * @param description human-readable description
 * @param feature product feature
 * @param component application or system component
 * @param coveredTestCases external or manual test-case references
 */
public record TestMetadata(
        String description,
        String feature,
        String component,
        List<TestCaseReference> coveredTestCases
) {

    public static final TestMetadata EMPTY =
            new TestMetadata(null, null, null, List.of());

    /**
     * Creates and normalizes optional test metadata.
     */
    public TestMetadata {
        description = normalize(description);
        feature = normalize(feature);
        component = normalize(component);

        if (coveredTestCases == null || coveredTestCases.isEmpty()) {
            coveredTestCases = List.of();
        } else {
            for (TestCaseReference reference : coveredTestCases) {
                Objects.requireNonNull(
                        reference,
                        "coveredTestCases contains null"
                );
            }
            coveredTestCases = List.copyOf(coveredTestCases);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
