package com.bavykin.qualistrata.core.telemetry;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable framework telemetry event.
 *
 * <p>This model belongs to {@code qualistrata-core} so runtime modules can emit
 * lightweight measurements without depending on the heavier
 * {@code qualistrata-perf} implementation module. The event is intentionally
 * generic: UI, DB, Unix, API, and consumer-project code all use the same shape.</p>
 *
 * <p>Tags are intended for low-cardinality dimensions such as module, target,
 * host alias, suite, or operation type. Fields are intended for measured values
 * and verbose diagnostics such as duration, exit code, byte counts, or error
 * type.</p>
 *
 * @param name metric or event name.
 * @param type event type.
 * @param timestamp event creation timestamp.
 * @param tags low-cardinality dimensions.
 * @param fields measured values and diagnostics.
 */
public record TelemetryEvent(
        String name,
        TelemetryEventType type,
        Instant timestamp,
        Map<String, String> tags,
        Map<String, Object> fields
) {

    /**
     * Creates a validated telemetry event and defensively copies maps.
     */
    public TelemetryEvent {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Telemetry event name must not be blank.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Telemetry event type must not be null.");
        }

        if (timestamp == null) {
            throw new IllegalArgumentException("Telemetry event timestamp must not be null.");
        }

        tags = copyStringMap(tags);
        fields = copyObjectMap(fields);
    }

    /**
     * Starts building a telemetry event with the current timestamp.
     *
     * @param name metric or event name.
     * @param type event type.
     * @return mutable builder.
     */
    public static Builder builder(String name, TelemetryEventType type) {
        return new Builder(name, type);
    }

    private static Map<String, String> copyStringMap(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }

        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static Map<String, Object> copyObjectMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }

        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    /**
     * Builder used to keep telemetry creation readable at instrumentation sites.
     */
    public static final class Builder {

        private final String name;
        private final TelemetryEventType type;
        private Instant timestamp = Instant.now();
        private final Map<String, String> tags = new LinkedHashMap<>();
        private final Map<String, Object> fields = new LinkedHashMap<>();

        private Builder(String name, TelemetryEventType type) {
            this.name = name;
            this.type = type;
        }

        /**
         * Overrides event timestamp, primarily for deterministic tests.
         *
         * @param value timestamp to use.
         * @return this builder.
         */
        public Builder timestamp(Instant value) {
            this.timestamp = value;
            return this;
        }

        /**
         * Adds a tag when the value is not {@code null}.
         *
         * @param key tag key.
         * @param value tag value.
         * @return this builder.
         */
        public Builder tag(String key, String value) {
            if (value != null) {
                tags.put(requireKey(key, "tag"), value);
            }

            return this;
        }

        /**
         * Adds a field when the value is not {@code null}.
         *
         * @param key field key.
         * @param value field value.
         * @return this builder.
         */
        public Builder field(String key, Object value) {
            if (value != null) {
                fields.put(requireKey(key, "field"), value);
            }

            return this;
        }

        /**
         * Creates immutable telemetry event.
         *
         * @return telemetry event.
         */
        public TelemetryEvent build() {
            return new TelemetryEvent(name, type, timestamp, tags, fields);
        }

        private String requireKey(String key, String type) {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Telemetry " + type + " key must not be blank.");
            }

            return key;
        }
    }
}
