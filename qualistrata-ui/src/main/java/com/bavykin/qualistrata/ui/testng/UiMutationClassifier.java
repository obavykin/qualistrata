package com.bavykin.qualistrata.ui.testng;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves UI mutation classification from framework annotations and TestNG groups.
 *
 * <p>The class keeps TestNG out of its API by accepting group names as strings.
 * Annotations have the same meaning as the conventional group names from
 * {@link UiTestGroups}. When multiple mutation types are declared, the most
 * restrictive type wins, but the result is marked as conflicting so CI can fail
 * fast through {@link UiMutationClassificationValidator}.</p>
 */
public final class UiMutationClassifier {

    /**
     * Creates a UiMutationClassifier instance.
     */
    public UiMutationClassifier() {
    }

    /**
     * Resolves mutation type for method and declaring class.
     *
     * @param method test method.
     * @return mutation type or {@code null} when not classified.
     */
    public UiMutationType classify(Method method) {
        return classification(method).type();
    }

    /**
     * Resolves mutation type from method annotations and group names.
     *
     * @param method test method.
     * @param groups TestNG group names, may be empty.
     * @return mutation type or {@code null} when not classified.
     */
    public UiMutationType classify(Method method, String[] groups) {
        return classification(method, groups).type();
    }

    /**
     * Resolves full mutation metadata for method and declaring class.
     *
     * @param method test method.
     * @return full classification result.
     */
    public UiMutationClassification classification(Method method) {
        return classification(method, new String[0]);
    }

    /**
     * Resolves full mutation metadata from annotations and group names.
     *
     * @param method test method.
     * @param groups TestNG group names, may be empty.
     * @return full classification result.
     */
    public UiMutationClassification classification(Method method, String[] groups) {
        if (method == null) {
            throw new IllegalArgumentException("Test method must not be null.");
        }

        Map<UiMutationType, List<String>> found = new LinkedHashMap<>();
        collectAnnotation(found, method, RequiresExclusiveEnvironment.class, UiMutationType.EXCLUSIVE);
        collectAnnotation(found, method.getDeclaringClass(), RequiresExclusiveEnvironment.class, UiMutationType.EXCLUSIVE);
        collectAnnotation(found, method, MutatingUiTest.class, UiMutationType.MUTATING);
        collectAnnotation(found, method.getDeclaringClass(), MutatingUiTest.class, UiMutationType.MUTATING);
        collectAnnotation(found, method, NonMutatingUiTest.class, UiMutationType.NON_MUTATING);
        collectAnnotation(found, method.getDeclaringClass(), NonMutatingUiTest.class, UiMutationType.NON_MUTATING);
        collectGroups(found, groups);

        UiMutationType type = mostRestrictive(found);
        List<String> sources = found.values().stream().flatMap(List::stream).toList();
        return new UiMutationClassification(type, sources, found.size() > 1);
    }

    private void collectAnnotation(
            Map<UiMutationType, List<String>> found,
            Method method,
            Class<? extends Annotation> annotationType,
            UiMutationType type
    ) {
        if (method.isAnnotationPresent(annotationType)) {
            add(found, type, "method @" + annotationType.getSimpleName());
        }
    }

    private void collectAnnotation(
            Map<UiMutationType, List<String>> found,
            Class<?> clazz,
            Class<? extends Annotation> annotationType,
            UiMutationType type
    ) {
        if (clazz.isAnnotationPresent(annotationType)) {
            add(found, type, "class @" + annotationType.getSimpleName());
        }
    }

    private void collectGroups(Map<UiMutationType, List<String>> found, String[] groups) {
        Arrays.stream(groups == null ? new String[0] : groups)
                .map(String::trim)
                .forEach(group -> {
                    if (UiTestGroups.EXCLUSIVE.equals(group)) {
                        add(found, UiMutationType.EXCLUSIVE, "group " + UiTestGroups.EXCLUSIVE);
                    } else if (UiTestGroups.MUTATING.equals(group)) {
                        add(found, UiMutationType.MUTATING, "group " + UiTestGroups.MUTATING);
                    } else if (UiTestGroups.NON_MUTATING.equals(group)) {
                        add(found, UiMutationType.NON_MUTATING, "group " + UiTestGroups.NON_MUTATING);
                    }
                });
    }

    private void add(Map<UiMutationType, List<String>> found, UiMutationType type, String source) {
        found.computeIfAbsent(type, ignored -> new ArrayList<>()).add(source);
    }

    private UiMutationType mostRestrictive(Map<UiMutationType, List<String>> found) {
        if (found.containsKey(UiMutationType.EXCLUSIVE)) {
            return UiMutationType.EXCLUSIVE;
        }
        if (found.containsKey(UiMutationType.MUTATING)) {
            return UiMutationType.MUTATING;
        }
        if (found.containsKey(UiMutationType.NON_MUTATING)) {
            return UiMutationType.NON_MUTATING;
        }
        return null;
    }
}
