package com.bavykin.qualistrata.ui.junit;

import java.util.List;

/**
 * Full mutation-classification result for one JUnit UI test method.
 *
 * @param type resolved mutation type.
 * @param sources classification sources that contributed to the result.
 * @param conflicting whether conflicting mutation markers were detected.
 */
public record UiJunitMutationClassification(
        UiJunitMutationType type,
        List<String> sources,
        boolean conflicting
) {
    /** Member. */
    public UiJunitMutationClassification {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}
