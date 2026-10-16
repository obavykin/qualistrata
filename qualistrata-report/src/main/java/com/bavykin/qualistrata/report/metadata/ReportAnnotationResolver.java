package com.bavykin.qualistrata.report.metadata;

import com.bavykin.qualistrata.report.annotation.Component;
import com.bavykin.qualistrata.report.annotation.CoversTestCase;
import com.bavykin.qualistrata.report.annotation.Feature;
import com.bavykin.qualistrata.report.annotation.TestDescription;
import com.bavykin.qualistrata.report.model.TestCaseReference;
import com.bavykin.qualistrata.report.model.TestMetadata;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves QualiStrata report annotations from a test class and method.
 *
 * <p>Method annotations override class annotations for scalar metadata.
 * Coverage references are combined in class-then-method order.</p>
 */
public final class ReportAnnotationResolver {

    /**
     * Creates an annotation resolver.
     */
    public ReportAnnotationResolver() {
    }

    /**
     * Resolves report metadata from the supplied test class and method.
     *
     * @param testClass test implementation class
     * @param testMethod test method, or {@code null} for class-only metadata
     * @return resolved framework-neutral test metadata
     */
    public TestMetadata resolve(Class<?> testClass, Method testMethod) {
        String description = testDescription(testClass, testMethod);
        String feature = feature(testClass, testMethod);
        String component = component(testClass, testMethod);

        List<TestCaseReference> references = new ArrayList<>();
        addReferences(references, testClass);
        addReferences(references, testMethod);

        return new TestMetadata(
                description,
                feature,
                component,
                references
        );
    }

    private static String testDescription(
            Class<?> testClass,
            Method testMethod
    ) {
        TestDescription methodAnnotation = annotation(
                testMethod,
                TestDescription.class
        );
        if (methodAnnotation != null) {
            return methodAnnotation.value();
        }
        TestDescription classAnnotation = annotation(
                testClass,
                TestDescription.class
        );
        return classAnnotation == null ? null : classAnnotation.value();
    }

    private static String feature(Class<?> testClass, Method testMethod) {
        Feature methodAnnotation = annotation(testMethod, Feature.class);
        if (methodAnnotation != null) {
            return methodAnnotation.value();
        }
        Feature classAnnotation = annotation(testClass, Feature.class);
        return classAnnotation == null ? null : classAnnotation.value();
    }

    private static String component(Class<?> testClass, Method testMethod) {
        Component methodAnnotation = annotation(testMethod, Component.class);
        if (methodAnnotation != null) {
            return methodAnnotation.value();
        }
        Component classAnnotation = annotation(testClass, Component.class);
        return classAnnotation == null ? null : classAnnotation.value();
    }

    private static <A extends java.lang.annotation.Annotation> A annotation(
            AnnotatedElement element,
            Class<A> annotationType
    ) {
        return element == null ? null : element.getAnnotation(annotationType);
    }

    private static void addReferences(
            List<TestCaseReference> references,
            AnnotatedElement element
    ) {
        if (element == null) {
            return;
        }

        for (CoversTestCase annotation
                : element.getAnnotationsByType(CoversTestCase.class)) {
            references.add(new TestCaseReference(
                    annotation.value(),
                    annotation.system()
            ));
        }
    }
}
