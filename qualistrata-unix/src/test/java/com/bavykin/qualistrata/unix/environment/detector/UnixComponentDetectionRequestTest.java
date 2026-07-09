package com.bavykin.qualistrata.unix.environment.detector;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link UnixComponentDetectionRequest}.
 */
class UnixComponentDetectionRequestTest {

    @Test
    void shouldCreateDetectionRequest() {
        UnixComponentDetectionRequest request = new UnixComponentDetectionRequest(
                " python-package ",
                " 6.0.4 ",
                " /opt/app/.profile ",
                Map.of("package", "Django")
        );

        assertEquals("python-package", request.detector());
        assertEquals("6.0.4", request.expectedVersion());
        assertEquals("/opt/app/.profile", request.shellProfile());
        assertEquals("Django", request.properties().get("package"));
    }

    @Test
    void shouldNormalizeBlankShellProfileToNull() {
        UnixComponentDetectionRequest request = new UnixComponentDetectionRequest(
                "python",
                "3.12.3",
                "   ",
                null
        );

        assertEquals(null, request.shellProfile());
        assertEquals(Map.of(), request.properties());
    }

    @Test
    void shouldReturnRequiredProperty() {
        UnixComponentDetectionRequest request = new UnixComponentDetectionRequest(
                "python-package",
                "6.0.4",
                null,
                Map.of("package", "Django")
        );

        assertEquals("Django", request.requiredProperty("package"));
    }

    @Test
    void shouldFailWhenDetectorIdIsBlank() {
        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> new UnixComponentDetectionRequest(" ", "1.0.0", null, Map.of())
        );

        assertEquals("Unix component detector id must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenExpectedVersionIsBlank() {
        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> new UnixComponentDetectionRequest("python", " ", null, Map.of())
        );

        assertEquals("Expected Unix component version must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenRequiredPropertyIsMissing() {
        UnixComponentDetectionRequest request = new UnixComponentDetectionRequest(
                "python-package",
                "6.0.4",
                null,
                Map.of()
        );

        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> request.requiredProperty("package")
        );

        assertEquals(
                "Required Unix component detector property is missing or blank: package",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenRequiredPropertyIsBlank() {
        UnixComponentDetectionRequest request = new UnixComponentDetectionRequest(
                "python-package",
                "6.0.4",
                null,
                Map.of("package", " ")
        );

        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> request.requiredProperty("package")
        );

        assertEquals(
                "Required Unix component detector property is missing or blank: package",
                exception.getMessage()
        );
    }
}