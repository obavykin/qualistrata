package com.bavykin.qualistrata.ui.junit;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UiJunitMutationClassificationValidatorTest {

    @Test
    void allowsUnclassifiedTestsByDefault() throws Exception {
        UiJunitMutationClassificationValidator validator = new UiJunitMutationClassificationValidator(
                new UiJunitMutationClassifier(),
                UiJunitUnclassifiedTestPolicy.ALLOW
        );

        assertDoesNotThrow(() -> validator.validate(method("unclassifiedScenario"), Set.of()));
    }

    @Test
    void failsUnclassifiedTestsWhenPolicyRequiresIt() throws Exception {
        UiJunitMutationClassificationValidator validator = new UiJunitMutationClassificationValidator(
                new UiJunitMutationClassifier(),
                UiJunitUnclassifiedTestPolicy.FAIL
        );

        assertThrows(
                UiJunitMutationValidationException.class,
                () -> validator.validate(method("unclassifiedScenario"), Set.of())
        );
    }

    @Test
    void failsConflictingClassification() throws Exception {
        UiJunitMutationClassificationValidator validator = new UiJunitMutationClassificationValidator(
                new UiJunitMutationClassifier(),
                UiJunitUnclassifiedTestPolicy.ALLOW
        );

        assertThrows(
                UiJunitMutationValidationException.class,
                () -> validator.validate(method("unclassifiedScenario"), Set.of(
                        UiTestTags.NON_MUTATING,
                        UiTestTags.MUTATING
                ))
        );
    }

    private Method method(String name) throws NoSuchMethodException {
        return Fixtures.class.getDeclaredMethod(name);
    }

    private static final class Fixtures {
        void unclassifiedScenario() {
            // Fixture method.
        }
    }
}
