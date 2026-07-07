package com.bavykin.qualistrata.ui.testng;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void shouldReturnNullWhenNotClassified() throws Exception {
        assertNull(classifier.classify(method("unclassified")));
    }

    @Test
    void shouldAllowOnlyNonMutatingParallelExecution() {
        UiParallelExecutionPolicy policy = new UiParallelExecutionPolicy();

        assertTrue(policy.mayRunInParallel(UiMutationType.NON_MUTATING));
        assertTrue(policy.requiresSequentialRun(UiMutationType.MUTATING));
        assertTrue(policy.requiresSequentialRun(UiMutationType.EXCLUSIVE));
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
