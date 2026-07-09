package com.bavykin.qualistrata.ui.testng;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for UI mutation classification.
 */
class UiMutationClassifierTest {

    private final UiMutationClassifier classifier = new UiMutationClassifier();

    @Test
    void shouldClassifyMethodLevelAnnotations() throws Exception {
        assertEquals(UiMutationType.NON_MUTATING, classifier.classify(method("readOnly")));
        assertEquals(UiMutationType.MUTATING, classifier.classify(method("mutating")));
        assertEquals(UiMutationType.EXCLUSIVE, classifier.classify(method("exclusive")));
    }

    @Test
    void shouldClassifyConventionalTestNgGroups() throws Exception {
        assertEquals(
                UiMutationType.NON_MUTATING,
                classifier.classify(method("unclassified"), new String[] {UiTestGroups.NON_MUTATING})
        );
        assertEquals(
                UiMutationType.MUTATING,
                classifier.classify(method("unclassified"), new String[] {UiTestGroups.MUTATING})
        );
        assertEquals(
                UiMutationType.EXCLUSIVE,
                classifier.classify(method("unclassified"), new String[] {UiTestGroups.EXCLUSIVE})
        );
    }

    @Test
    void shouldReturnNullWhenNotClassified() throws Exception {
        assertNull(classifier.classify(method("unclassified")));
        assertTrue(classifier.classification(method("unclassified")).unclassified());
    }

    @Test
    void shouldMarkConflictingClassification() throws Exception {
        UiMutationClassification classification = classifier.classification(
                method("readOnly"),
                new String[] {UiTestGroups.MUTATING}
        );

        assertEquals(UiMutationType.MUTATING, classification.type());
        assertTrue(classification.conflicts());
    }

    @Test
    void shouldAllowOnlyNonMutatingParallelExecution() {
        UiParallelExecutionPolicy policy = new UiParallelExecutionPolicy();

        assertTrue(policy.mayRunInParallel(UiMutationType.NON_MUTATING));
        assertTrue(policy.requiresSequentialRun(UiMutationType.MUTATING));
        assertTrue(policy.requiresSequentialRun(UiMutationType.EXCLUSIVE));
        assertEquals(UiTestGroups.NON_MUTATING, policy.groupFor(UiMutationType.NON_MUTATING));
    }

    @Test
    void validatorShouldFailUnclassifiedTestsWhenPolicyRequiresIt() throws Exception {
        UiMutationClassificationValidator validator = new UiMutationClassificationValidator(
                UiUnclassifiedTestPolicy.FAIL
        );

        assertThrows(
                UiMutationValidationException.class,
                () -> validator.validate(method("unclassified"), new String[0])
        );
    }

    @Test
    void validatorShouldAllowUnclassifiedTestsByDefault() throws Exception {
        UiMutationClassificationValidator validator = new UiMutationClassificationValidator(
                UiUnclassifiedTestPolicy.ALLOW
        );

        UiMutationClassification classification = validator.validate(method("unclassified"), new String[0]);

        assertTrue(classification.unclassified());
        assertFalse(classification.conflicts());
    }

    @Test
    void validatorShouldFailConflictingClassification() throws Exception {
        UiMutationClassificationValidator validator = new UiMutationClassificationValidator(
                UiUnclassifiedTestPolicy.ALLOW
        );

        assertThrows(
                UiMutationValidationException.class,
                () -> validator.validate(method("readOnly"), new String[] {UiTestGroups.MUTATING})
        );
    }

    @Test
    void shouldParseUnclassifiedPolicyValues() {
        assertEquals(UiUnclassifiedTestPolicy.ALLOW, UiUnclassifiedTestPolicy.from(null));
        assertEquals(UiUnclassifiedTestPolicy.WARN, UiUnclassifiedTestPolicy.from("warn"));
        assertEquals(UiUnclassifiedTestPolicy.FAIL, UiUnclassifiedTestPolicy.from("required"));
    }

    private Method method(String name) throws Exception {
        return Fixture.class.getDeclaredMethod(name);
    }

    private static final class Fixture {
        @NonMutatingUiTest void readOnly() { }
        @MutatingUiTest void mutating() { }
        @RequiresExclusiveEnvironment void exclusive() { }
        void unclassified() { }
    }
}
