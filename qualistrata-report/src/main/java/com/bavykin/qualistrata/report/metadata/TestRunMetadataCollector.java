package com.bavykin.qualistrata.report.metadata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Deterministically loads and merges test-run metadata providers.
 *
 * <p>Providers are ordered by implementation class name. Duplicate keys are
 * accepted only when both providers contribute equal values. Conflicting values
 * fail explicitly instead of being resolved by an unstable loader order.</p>
 */
public final class TestRunMetadataCollector {

    private final List<TestRunMetadataProvider> providers;

    /**
     * Creates a collector from an explicit provider collection.
     *
     * @param providers metadata providers
     */
    public TestRunMetadataCollector(
            Iterable<? extends TestRunMetadataProvider> providers
    ) {
        Objects.requireNonNull(providers, "providers");

        List<TestRunMetadataProvider> copy = new ArrayList<>();
        for (TestRunMetadataProvider provider : providers) {
            copy.add(Objects.requireNonNull(
                    provider,
                    "providers contains null"
            ));
        }
        copy.sort(Comparator.comparing(
                provider -> provider.getClass().getName()
        ));
        this.providers = List.copyOf(copy);
    }

    /**
     * Loads providers through {@link ServiceLoader} using the context class loader.
     *
     * @return deterministic metadata collector
     * @throws MetadataCollectionException when provider discovery fails
     */
    public static TestRunMetadataCollector load() {
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        return load(classLoader);
    }

    /**
     * Loads providers through {@link ServiceLoader} using the supplied class loader.
     *
     * @param classLoader provider class loader
     * @return deterministic metadata collector
     * @throws MetadataCollectionException when provider discovery fails
     */
    public static TestRunMetadataCollector load(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader");

        try {
            ServiceLoader<TestRunMetadataProvider> loader =
                    ServiceLoader.load(
                            TestRunMetadataProvider.class,
                            classLoader
                    );
            List<TestRunMetadataProvider> providers = new ArrayList<>();
            loader.forEach(providers::add);
            return new TestRunMetadataCollector(providers);
        } catch (ServiceConfigurationError error) {
            throw new MetadataCollectionException(
                    "Unable to load test-run metadata providers",
                    error
            );
        }
    }

    /**
     * Collects and merges metadata from every provider.
     *
     * @return immutable merged metadata
     * @throws MetadataCollectionException when a provider fails or keys conflict
     */
    public Map<String, Object> collect() {
        LinkedHashMap<String, Object> merged = new LinkedHashMap<>();

        for (TestRunMetadataProvider provider : providers) {
            Map<String, Object> metadata = providerMetadata(provider);

            metadata.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> merge(
                            merged,
                            provider,
                            entry.getKey(),
                            entry.getValue()
                    ));
        }

        return Map.copyOf(merged);
    }

    private static Map<String, Object> providerMetadata(
            TestRunMetadataProvider provider
    ) {
        try {
            Map<String, Object> metadata = provider.metadata();
            if (metadata == null) {
                throw new MetadataCollectionException(
                        "Metadata provider "
                                + provider.getClass().getName()
                                + " returned null"
                );
            }
            return metadata;
        } catch (MetadataCollectionException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new MetadataCollectionException(
                    "Metadata provider "
                            + provider.getClass().getName()
                            + " failed",
                    exception
            );
        }
    }

    private static void merge(
            Map<String, Object> merged,
            TestRunMetadataProvider provider,
            String key,
            Object value
    ) {
        String normalizedKey = requireKey(key, provider);

        if (!merged.containsKey(normalizedKey)) {
            merged.put(normalizedKey, value);
            return;
        }

        Object existing = merged.get(normalizedKey);
        if (!Objects.equals(existing, value)) {
            throw new MetadataCollectionException(
                    "Conflicting metadata value for key '"
                            + normalizedKey
                            + "' contributed by "
                            + provider.getClass().getName()
            );
        }
    }

    private static String requireKey(
            String key,
            TestRunMetadataProvider provider
    ) {
        if (key == null || key.trim().isEmpty()) {
            throw new MetadataCollectionException(
                    "Metadata provider "
                            + provider.getClass().getName()
                            + " contributed a blank key"
            );
        }
        return key.trim();
    }
}
