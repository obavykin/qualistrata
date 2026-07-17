package com.bavykin.qualistrata.report.metadata;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestRunMetadataCollectorTest {

    @Test
    void mergesProvidersDeterministicallyByProviderAndKey() {
        TestRunMetadataCollector collector =
                new TestRunMetadataCollector(List.of(
                        new ZEnvironmentProvider(),
                        new AApplicationProvider()
                ));

        assertEquals(
                Map.of(
                        "application", "TNT",
                        "environment", "pt-b",
                        "release", "6.6"
                ),
                collector.collect()
        );
    }

    @Test
    void allowsEqualDuplicateValues() {
        TestRunMetadataCollector collector =
                new TestRunMetadataCollector(List.of(
                        provider(Map.of("application", "TNT")),
                        provider(Map.of("application", "TNT"))
                ));

        assertEquals(
                Map.of("application", "TNT"),
                collector.collect()
        );
    }

    @Test
    void rejectsConflictingDuplicateValues() {
        TestRunMetadataCollector collector =
                new TestRunMetadataCollector(List.of(
                        provider(Map.of("environment", "pt-a")),
                        provider(Map.of("environment", "pt-b"))
                ));

        assertThrows(
                MetadataCollectionException.class,
                collector::collect
        );
    }

    @Test
    void rejectsBlankMetadataKeys() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put(" ", "value");

        TestRunMetadataCollector collector =
                new TestRunMetadataCollector(List.of(provider(metadata)));

        assertThrows(
                MetadataCollectionException.class,
                collector::collect
        );
    }

    @Test
    void wrapsProviderFailure() {
        TestRunMetadataProvider failing = () -> {
            throw new IllegalStateException("boom");
        };

        TestRunMetadataCollector collector =
                new TestRunMetadataCollector(List.of(failing));

        MetadataCollectionException exception = assertThrows(
                MetadataCollectionException.class,
                collector::collect
        );

        assertEquals(
                IllegalStateException.class,
                exception.getCause().getClass()
        );
    }

    @Test
    void loadsProvidersFromTheThreadContextClassLoader() {
        Thread thread = Thread.currentThread();
        ClassLoader originalClassLoader = thread.getContextClassLoader();

        try {
            thread.setContextClassLoader(
                    new ClassLoader(originalClassLoader) {
                    }
            );

            assertEquals(
                    Map.of(),
                    TestRunMetadataCollector.load().collect()
            );
        } finally {
            thread.setContextClassLoader(originalClassLoader);
        }
    }

    @Test
    void returnsImmutableMetadata() {
        Map<String, Object> metadata =
                new TestRunMetadataCollector(List.of(
                        provider(Map.of("application", "TNT"))
                )).collect();

        assertThrows(
                UnsupportedOperationException.class,
                () -> addMetadata(metadata)
        );
    }

    private static void addMetadata(Map<String, Object> metadata) {
        metadata.put("environment", "pt-b");
    }

    private static TestRunMetadataProvider provider(
            Map<String, Object> metadata
    ) {
        return () -> metadata;
    }

    private static final class AApplicationProvider
            implements TestRunMetadataProvider {

        @Override
        public Map<String, Object> metadata() {
            return Map.of("application", "TNT");
        }
    }

    private static final class ZEnvironmentProvider
            implements TestRunMetadataProvider {

        @Override
        public Map<String, Object> metadata() {
            LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("release", "6.6");
            metadata.put("environment", "pt-b");
            return metadata;
        }
    }
}
