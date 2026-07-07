package com.bavykin.qualistrata.ui.testng;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Resolves UI mutation classification from framework annotations.
 *
 * <p>The class is independent from TestNG APIs. TestNG/JUnit/Gradle listeners
 * can use it without forcing a particular test runner dependency on the core UI
 * runtime.</p>
 */
public final class UiMutationClassifier {

    /**
     * Resolves mutation type for method and declaring class.
     *
     * @param method test method.
     * @return mutation type or {@code null} when not classified.
     */
    public UiMutationType classify(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("Test method must not be null.");
        }

        if (has(method, RequiresExclusiveEnvironment.class) || has(method.getDeclaringClass(), RequiresExclusiveEnvironment.class)) {
            return UiMutationType.EXCLUSIVE;
        }
        if (has(method, MutatingUiTest.class) || has(method.getDeclaringClass(), MutatingUiTest.class)) {
            return UiMutationType.MUTATING;
        }
        if (has(method, NonMutatingUiTest.class) || has(method.getDeclaringClass(), NonMutatingUiTest.class)) {
            return UiMutationType.NON_MUTATING;
        }

        return null;
    }

    private boolean has(Method method, Class<? extends Annotation> annotationType) {
        return method.isAnnotationPresent(annotationType);
    }

    private boolean has(Class<?> type, Class<? extends Annotation> annotationType) {
        return type.isAnnotationPresent(annotationType);
    }
}
