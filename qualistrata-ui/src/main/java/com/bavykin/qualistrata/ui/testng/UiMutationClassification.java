package com.bavykin.qualistrata.ui.testng;

import java.util.List;

/**
 * Result of resolving mutation metadata for one UI test method.
 *
 * @param type resolved mutation type, or {@code null} when the test is unclassified.
 * @param sources human-readable sources that contributed the classification.
 * @param conflicts whether more than one mutation type was declared.
 */
public record UiMutationClassification(
        UiMutationType type,
        List<String> sources,
        boolean conflicts
) {
    /** Creates a validated immutable classification result. */
    public UiMutationClassification {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }

    /** @return whether the test has no mutation metadata. */
    public boolean unclassified() {
        return type == null;
    }

    /** @return mutation type name or {@code null} for JSON/report compatibility. */
    public String typeName() {
        return type == null ? null : type.name();
    }
}
