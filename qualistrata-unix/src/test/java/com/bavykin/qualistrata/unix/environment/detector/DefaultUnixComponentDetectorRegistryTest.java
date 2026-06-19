package com.bavykin.qualistrata.unix.environment.detector;

import com.bavykin.qualistrata.unix.environment.detector.impl.ApacheVersionDetector;
import com.bavykin.qualistrata.unix.environment.detector.impl.OracleClientVersionDetector;
import com.bavykin.qualistrata.unix.environment.detector.impl.PythonPackageVersionDetector;
import com.bavykin.qualistrata.unix.environment.detector.impl.PythonVersionDetector;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DefaultUnixComponentDetectorRegistry}.
 */
class DefaultUnixComponentDetectorRegistryTest {

    @Test
    void shouldRegisterDefaultDetectors() {
        DefaultUnixComponentDetectorRegistry registry = new DefaultUnixComponentDetectorRegistry();

        assertEquals(PythonVersionDetector.ID, registry.detector("python").id());
        assertEquals(PythonPackageVersionDetector.ID, registry.detector("python-package").id());
        assertEquals(ApacheVersionDetector.ID, registry.detector("apache").id());
        assertEquals(OracleClientVersionDetector.ID, registry.detector("oracle-client").id());
    }

    @Test
    void shouldFailWhenDetectorIdIsBlank() {
        DefaultUnixComponentDetectorRegistry registry = new DefaultUnixComponentDetectorRegistry();

        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> registry.detector(" ")
        );

        assertEquals("Unix component detector id must not be blank.", exception.getMessage());
    }

    @Test
    void shouldFailWhenDetectorIsUnknown() {
        DefaultUnixComponentDetectorRegistry registry = new DefaultUnixComponentDetectorRegistry();

        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> registry.detector("unknown")
        );

        assertEquals("Unknown Unix component detector id: unknown", exception.getMessage());
    }

    @Test
    void shouldFailWhenRegisteredDetectorIsNull() {
        DefaultUnixComponentDetectorRegistry registry = new DefaultUnixComponentDetectorRegistry();

        UnixComponentDetectorException exception = assertThrows(
                UnixComponentDetectorException.class,
                () -> registry.register(null)
        );

        assertEquals("Unix component detector must not be null.", exception.getMessage());
    }

    @Test
    void shouldAllowCustomDetectorRegistration() {
        DefaultUnixComponentDetectorRegistry registry = new DefaultUnixComponentDetectorRegistry();

        registry.register(new StubDetector());

        assertEquals("custom", registry.detector("custom").id());
    }

    /**
     * Stub detector used to verify custom detector registration.
     */
    private static class StubDetector implements UnixComponentDetector {

        /**
         * Returns detector id.
         *
         * @return detector id.
         */
        @Override
        public String id() {
            return "custom";
        }

        /**
         * Stub detection method.
         *
         * @param unixFacade Unix facade.
         * @param hostAlias logical Unix host alias.
         * @param userAlias optional Unix user alias.
         * @param request detection request.
         * @param timeout command timeout.
         * @return detection result.
         */
        @Override
        public UnixComponentDetectionResult detect(
                com.bavykin.qualistrata.unix.facade.UnixFacade unixFacade,
                String hostAlias,
                String userAlias,
                UnixComponentDetectionRequest request,
                Duration timeout
        ) {
            throw new UnsupportedOperationException("Stub detector should not be executed.");
        }
    }
}