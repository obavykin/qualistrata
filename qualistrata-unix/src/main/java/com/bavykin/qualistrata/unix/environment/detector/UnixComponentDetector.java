package com.bavykin.qualistrata.unix.environment.detector;

import com.bavykin.qualistrata.unix.facade.UnixFacade;

import java.time.Duration;

/**
 * Generic Unix component detector.
 *
 * <p>A detector knows how to build and execute a command for one component
 * family, parse the component version, and compare it with the expected
 * baseline version.</p>
 */
public interface UnixComponentDetector {

    /**
     * Returns stable detector id used by consumer-side baseline files.
     *
     * @return detector id.
     */
    String id();

    /**
     * Detects a Unix component version.
     *
     * @param unixFacade Unix facade used to execute commands.
     * @param hostAlias logical Unix host alias.
     * @param userAlias optional Unix user alias.
     * @param request detection request.
     * @param timeout command timeout.
     * @return detection result.
     */
    UnixComponentDetectionResult detect(
            UnixFacade unixFacade,
            String hostAlias,
            String userAlias,
            UnixComponentDetectionRequest request,
            Duration timeout
    );
}