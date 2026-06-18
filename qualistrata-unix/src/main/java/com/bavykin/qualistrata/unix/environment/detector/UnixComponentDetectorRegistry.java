package com.bavykin.qualistrata.unix.environment.detector;

import java.util.Collection;

/**
 * Registry of Unix component detectors.
 */
public interface UnixComponentDetectorRegistry {

    /**
     * Resolves a detector by id.
     *
     * @param detectorId detector id.
     * @return registered detector.
     * @throws UnixComponentDetectorException if detector is unknown.
     */
    UnixComponentDetector detector(String detectorId);

    /**
     * Returns all registered detectors.
     *
     * @return registered detectors.
     */
    Collection<UnixComponentDetector> detectors();
}