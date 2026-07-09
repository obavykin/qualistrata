package com.bavykin.qualistrata.ui.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves mutation classification for JUnit 5 UI tests.
 *
 * <p>The classifier understands both QualiStrata JUnit annotations and plain
 * JUnit tags. This lets consumer projects either use readable annotations such
 * as {@link NonMutatingUiTest}, or keep their own annotations as long as they
 * emit the conventional tags from {@link UiTestTags}.</p>
 */
public final class UiJunitMutationClassifier {

    /** Resolves full mutation metadata for a JUnit test method. */
    public UiJunitMutationClassification classification(Method method, Set<String> tags) {
        if (method == null) {
            throw new IllegalArgumentException("Test method must not be null.");
        }

        Map<UiJunitMutationType, List<String>> found = new LinkedHashMap<>();
        collectAnnotation(found, method, RequiresExclusiveEnvironment.class, UiJunitMutationType.EXCLUSIVE);
        collectAnnotation(found, method.getDeclaringClass(), RequiresExclusiveEnvironment.class, UiJunitMutationType.EXCLUSIVE);
        collectAnnotation(found, method, MutatingUiTest.class, UiJunitMutationType.MUTATING);
        collectAnnotation(found, method.getDeclaringClass(), MutatingUiTest.class, UiJunitMutationType.MUTATING);
        collectAnnotation(found, method, NonMutatingUiTest.class, UiJunitMutationType.NON_MUTATING);
        collectAnnotation(found, method.getDeclaringClass(), NonMutatingUiTest.class, UiJunitMutationType.NON_MUTATING);
        collectTags(found, tags);

        UiJunitMutationType type = mostRestrictive(found);
        List<String> sources = found.values().stream().flatMap(List::stream).toList();
        return new UiJunitMutationClassification(type, sources, found.size() > 1);
    }

    /** Resolves only the final mutation type. */
    public UiJunitMutationType classify(Method method, Set<String> tags) {
        return classification(method, tags).type();
    }

    private void collectAnnotation(
            Map<UiJunitMutationType, List<String>> found,
            Method method,
            Class<? extends Annotation> annotationType,
            UiJunitMutationType type
    ) {
        if (method.isAnnotationPresent(annotationType)) {
            add(found, type, "method @" + annotationType.getSimpleName());
        }
    }

    private void collectAnnotation(
            Map<UiJunitMutationType, List<String>> found,
            Class<?> clazz,
            Class<? extends Annotation> annotationType,
            UiJunitMutationType type
    ) {
        if (clazz.isAnnotationPresent(annotationType)) {
            add(found, type, "class @" + annotationType.getSimpleName());
        }
    }

    private void collectTags(Map<UiJunitMutationType, List<String>> found, Set<String> tags) {
        for (String tag : tags == null ? Set.<String>of() : tags) {
            if (UiTestTags.EXCLUSIVE.equals(tag)) {
                add(found, UiJunitMutationType.EXCLUSIVE, "tag " + UiTestTags.EXCLUSIVE);
            } else if (UiTestTags.MUTATING.equals(tag)) {
                add(found, UiJunitMutationType.MUTATING, "tag " + UiTestTags.MUTATING);
            } else if (UiTestTags.NON_MUTATING.equals(tag)) {
                add(found, UiJunitMutationType.NON_MUTATING, "tag " + UiTestTags.NON_MUTATING);
            }
        }
    }

    private void add(Map<UiJunitMutationType, List<String>> found, UiJunitMutationType type, String source) {
        found.computeIfAbsent(type, ignored -> new ArrayList<>()).add(source);
    }

    private UiJunitMutationType mostRestrictive(Map<UiJunitMutationType, List<String>> found) {
        if (found.containsKey(UiJunitMutationType.EXCLUSIVE)) {
            return UiJunitMutationType.EXCLUSIVE;
        }
        if (found.containsKey(UiJunitMutationType.MUTATING)) {
            return UiJunitMutationType.MUTATING;
        }
        if (found.containsKey(UiJunitMutationType.NON_MUTATING)) {
            return UiJunitMutationType.NON_MUTATING;
        }
        return null;
    }
}
