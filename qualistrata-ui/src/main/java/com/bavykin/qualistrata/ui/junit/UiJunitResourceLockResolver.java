package com.bavykin.qualistrata.ui.junit;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Resolves effective environment and named-resource locks for one JUnit test. */
final class UiJunitResourceLockResolver {

    private static final String DEFAULT_TARGET = "<default>";

    List<UiJunitResourceLockRegistry.Request> resolve(
            Method method,
            String target,
            boolean exclusiveEnvironment
    ) {
        Objects.requireNonNull(method, "Test method must not be null.");
        String normalizedTarget = normalizeTarget(target);
        Map<String, SharedResourceAccessMode> resolved = new LinkedHashMap<>();

        resolved.put(
                environmentKey(normalizedTarget),
                exclusiveEnvironment ? SharedResourceAccessMode.WRITE : SharedResourceAccessMode.READ
        );

        collect(
                resolved,
                method.getDeclaringClass().getAnnotationsByType(SharedResourceLock.class),
                normalizedTarget
        );
        collect(
                resolved,
                method.getAnnotationsByType(SharedResourceLock.class),
                normalizedTarget
        );

        return resolved.entrySet().stream()
                .map(entry -> new UiJunitResourceLockRegistry.Request(entry.getKey(), entry.getValue()))
                .sorted(java.util.Comparator.comparing(UiJunitResourceLockRegistry.Request::key))
                .toList();
    }

    private void collect(
            Map<String, SharedResourceAccessMode> resolved,
            SharedResourceLock[] declarations,
            String target
    ) {
        Arrays.stream(declarations).forEach(declaration -> {
            String resource = normalizeResource(declaration.value());
            String key = declaration.scope() == SharedResourceScope.GLOBAL
                    ? globalKey(resource)
                    : targetKey(target, resource);
            resolved.merge(key, declaration.mode(), UiJunitResourceLockResolver::strongest);
        });
    }

    private static SharedResourceAccessMode strongest(
            SharedResourceAccessMode left,
            SharedResourceAccessMode right
    ) {
        return left == SharedResourceAccessMode.WRITE || right == SharedResourceAccessMode.WRITE
                ? SharedResourceAccessMode.WRITE
                : SharedResourceAccessMode.READ;
    }

    private String normalizeResource(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Shared resource name must not be blank.");
        }
        return value.trim();
    }

    private String normalizeTarget(String value) {
        return value == null || value.isBlank() ? DEFAULT_TARGET : value.trim();
    }

    private String environmentKey(String target) {
        return "00|TARGET|" + target + "|__environment__";
    }

    private String targetKey(String target, String resource) {
        return "10|TARGET|" + target + "|" + resource;
    }

    private String globalKey(String resource) {
        return "10|GLOBAL|" + resource;
    }
}
