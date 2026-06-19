package com.bavykin.qualistrata.unix.environment.detector;

import com.bavykin.qualistrata.unix.environment.detector.impl.ApacheVersionDetector;
import com.bavykin.qualistrata.unix.environment.detector.impl.OracleClientVersionDetector;
import com.bavykin.qualistrata.unix.environment.detector.impl.PythonPackageVersionDetector;
import com.bavykin.qualistrata.unix.environment.detector.impl.PythonVersionDetector;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default registry with generic built-in Unix component detectors.
 */
public class DefaultUnixComponentDetectorRegistry implements UnixComponentDetectorRegistry {

    private final Map<String, UnixComponentDetector> detectors = new LinkedHashMap<>();

    /**
     * Creates a registry with the default generic Unix detectors.
     */
    public DefaultUnixComponentDetectorRegistry() {
        register(new PythonVersionDetector());
        register(new PythonPackageVersionDetector());
        register(new ApacheVersionDetector());
        register(new OracleClientVersionDetector());
    }

    /**
     * Registers a detector.
     *
     * @param detector detector to register.
     */
    public final void register(UnixComponentDetector detector) {
        if (detector == null) {
            throw new UnixComponentDetectorException("Unix component detector must not be null.");
        }

        detectors.put(detector.id(), detector);
    }

    /**
     * Resolves a detector by id.
     *
     * @param detectorId detector id.
     * @return registered detector.
     */
    @Override
    public UnixComponentDetector detector(String detectorId) {
        if (detectorId == null || detectorId.isBlank()) {
            throw new UnixComponentDetectorException("Unix component detector id must not be blank.");
        }

        UnixComponentDetector detector = detectors.get(detectorId.trim());

        if (detector == null) {
            throw new UnixComponentDetectorException("Unknown Unix component detector id: " + detectorId.trim());
        }

        return detector;
    }

    /**
     * Returns all registered detectors.
     *
     * @return registered detectors.
     */
    @Override
    public Collection<UnixComponentDetector> detectors() {
        return detectors.values();
    }
}