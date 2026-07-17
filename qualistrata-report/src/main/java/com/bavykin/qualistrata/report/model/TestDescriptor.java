package com.bavykin.qualistrata.report.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable descriptive data for one concrete test invocation.
 *
 * @param identifier stable invocation identity
 * @param displayName human-readable framework display name
 * @param parameters safely serialized invocation parameters
 * @param tags framework tags or groups associated with the invocation
 * @param metadata optional annotation-derived test metadata
 */
public record TestDescriptor(
        TestIdentifier identifier,
        String displayName,
        Map<String, String> parameters,
        Set<String> tags,
        TestMetadata metadata
) {

    /**
     * Creates an immutable descriptor with defensive copies of collection values.
     */
    public TestDescriptor {
        Objects.requireNonNull(identifier, "identifier");
        displayName = requireText(displayName, "displayName");
        parameters = immutableParameters(parameters);
        tags = immutableTags(tags);
        metadata = metadata == null ? TestMetadata.EMPTY : metadata;
    }

    /**
     * Creates a descriptor without optional annotation-derived metadata.
     *
     * @param identifier stable invocation identity
     * @param displayName human-readable framework display name
     * @param parameters safely serialized invocation parameters
     * @param tags framework tags or groups associated with the invocation
     */
    public TestDescriptor(
            TestIdentifier identifier,
            String displayName,
            Map<String, String> parameters,
            Set<String> tags
    ) {
        this(identifier, displayName, parameters, tags, TestMetadata.EMPTY);
    }

    private static Map<String, String> immutableParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Map.of();
        }

        LinkedHashMap<String, String> copy = new LinkedHashMap<>();
        parameters.forEach((name, value) -> {
            String normalizedName = requireText(name, "parameter name");
            copy.put(normalizedName, value);
        });
        return Map.copyOf(copy);
    }

    private static Set<String> immutableTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }

        LinkedHashSet<String> copy = new LinkedHashSet<>();
        for (String tag : tags) {
            copy.add(requireText(tag, "tag"));
        }
        return Set.copyOf(copy);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
