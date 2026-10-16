package com.bavykin.qualistrata.ui.testng;

import java.lang.reflect.Method;
import java.util.StringJoiner;

/**
 * Applies CI policy to resolved UI mutation classification.
 */
public final class UiMutationClassificationValidator {

    private final UiMutationClassifier classifier;
    private final UiUnclassifiedTestPolicy unclassifiedPolicy;

    /**
     * Creates validator with explicit policy.
     *
     * @param unclassifiedPolicy unclassifiedPolicy value.
     */
    public UiMutationClassificationValidator(UiUnclassifiedTestPolicy unclassifiedPolicy) {
        this(new UiMutationClassifier(), unclassifiedPolicy);
    }

    /**
     * Creates validator with explicit collaborators.
     *
     * @param classifier classifier value.
     * @param unclassifiedPolicy unclassifiedPolicy value.
     */
    public UiMutationClassificationValidator(
            UiMutationClassifier classifier,
            UiUnclassifiedTestPolicy unclassifiedPolicy
    ) {
        this.classifier = classifier == null ? new UiMutationClassifier() : classifier;
        this.unclassifiedPolicy = unclassifiedPolicy == null ? UiUnclassifiedTestPolicy.ALLOW : unclassifiedPolicy;
    }

    /**
     * Resolves and validates classification for one test method.
     *
     * @param method test method.
     * @param groups TestNG groups.
     * @return resolved classification.
     */
    public UiMutationClassification validate(Method method, String[] groups) {
        UiMutationClassification classification = classifier.classification(method, groups);
        if (classification.conflicts()) {
            throw new UiMutationValidationException("Conflicting UI mutation classification for "
                    + describe(method) + ": " + String.join(", ", classification.sources()));
        }

        if (classification.unclassified()) {
            String message = "UI test has no mutation classification: " + describe(method)
                    + ". Add @NonMutatingUiTest, @MutatingUiTest, @RequiresExclusiveEnvironment, "
                    + "or one of the conventional TestNG groups from UiTestGroups.";
            if (unclassifiedPolicy == UiUnclassifiedTestPolicy.FAIL) {
                throw new UiMutationValidationException(message);
            }
            if (unclassifiedPolicy == UiUnclassifiedTestPolicy.WARN) {
                System.err.println("[QualiStrata UI] WARN: " + message);
            }
        }

        return classification;
    }

    private String describe(Method method) {
        if (method == null) {
            return "<unknown method>";
        }

        StringJoiner joiner = new StringJoiner("#");
        joiner.add(method.getDeclaringClass().getName());
        joiner.add(method.getName());
        return joiner.toString();
    }
}
