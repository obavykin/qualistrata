package com.bavykin.qualistrata.ui.junit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiJunitMutationClassifierTest {

    private final UiJunitMutationClassifier classifier = new UiJunitMutationClassifier();

    @Test
    void resolvesNonMutatingAnnotation() throws Exception {
        UiJunitMutationClassification classification = classifier.classification(
                method("readOnlyScenario"),
                Set.of(UiTestTags.UI, UiTestTags.NON_MUTATING)
        );

        assertEquals(UiJunitMutationType.NON_MUTATING, classification.type());
        assertTrue(classification.sources().stream().anyMatch(source -> source.contains("NonMutatingUiTest")));
    }

    @Test
    void resolvesMutatingTag() throws Exception {
        UiJunitMutationClassification classification = classifier.classification(
                method("plainTaggedScenario"),
                Set.of(UiTestTags.UI, UiTestTags.MUTATING)
        );

        assertEquals(UiJunitMutationType.MUTATING, classification.type());
    }

    @Test
    void usesMostRestrictiveClassificationWhenTagsConflict() throws Exception {
        UiJunitMutationClassification classification = classifier.classification(
                method("plainTaggedScenario"),
                Set.of(UiTestTags.NON_MUTATING, UiTestTags.EXCLUSIVE)
        );

        assertEquals(UiJunitMutationType.EXCLUSIVE, classification.type());
        assertTrue(classification.conflicting());
    }

    @Test
    void returnsNullForUnclassifiedTest() throws Exception {
        UiJunitMutationClassification classification = classifier.classification(
                method("unclassifiedScenario"),
                Set.of()
        );

        assertNull(classification.type());
    }

    private Method method(String name) throws NoSuchMethodException {
        return Fixtures.class.getDeclaredMethod(name);
    }

    private static final class Fixtures {

        @NonMutatingUiTest
        void readOnlyScenario() {
            // Fixture method.
        }

        @Tag(UiTestTags.MUTATING)
        void plainTaggedScenario() {
            // Fixture method.
        }

        void unclassifiedScenario() {
            // Fixture method.
        }
    }
}
