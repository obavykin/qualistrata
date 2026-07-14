package com.bavykin.qualistrata.ui.junit;

import java.lang.reflect.Method;
import java.util.Set;

/** Applies configured validation rules to JUnit UI mutation classification. */
public final class UiJunitMutationClassificationValidator {

    private final UiJunitMutationClassifier classifier;
    private final UiJunitUnclassifiedTestPolicy unclassifiedPolicy;

    /** Member. */
    /**
     * Creates a mutation-classification validator.
     *
     * @param classifier classifier used to resolve mutation metadata.
     * @param unclassifiedPolicy policy applied when no mutation metadata is present.
     */
    public UiJunitMutationClassificationValidator(
            UiJunitMutationClassifier classifier,
            UiJunitUnclassifiedTestPolicy unclassifiedPolicy
    ) {
        this.classifier = classifier == null ? new UiJunitMutationClassifier() : classifier;
        this.unclassifiedPolicy = unclassifiedPolicy == null ? UiJunitUnclassifiedTestPolicy.ALLOW : unclassifiedPolicy;
    }

    /**
     * Validates method classification and returns the resolved classification.
     *
     * @param method method value.
     * @param tags tags value.
     * @return the resulting value.
     */
    public UiJunitMutationClassification validate(Method method, Set<String> tags) {
        UiJunitMutationClassification classification = classifier.classification(method, tags);
        if (classification.conflicting()) {
            throw new UiJunitMutationValidationException(
                    "Conflicting UI mutation classification for " + method.getDeclaringClass().getName()
                            + "." + method.getName() + ": " + classification.sources()
            );
        }
        if (classification.type() == null && unclassifiedPolicy == UiJunitUnclassifiedTestPolicy.FAIL) {
            throw new UiJunitMutationValidationException(
                    "UI test is not classified as non-mutating, mutating, or exclusive: "
                            + method.getDeclaringClass().getName() + "." + method.getName()
            );
        }
        return classification;
    }
}
