package com.bavykin.qualistrata.ui.junit;

import java.util.List;

/** Full mutation-classification result for one JUnit UI test method. */
public record UiJunitMutationClassification(
        UiJunitMutationType type,
        List<String> sources,
        boolean conflicting
) {
    public UiJunitMutationClassification {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}
