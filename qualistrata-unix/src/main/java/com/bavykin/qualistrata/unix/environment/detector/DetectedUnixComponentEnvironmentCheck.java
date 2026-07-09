package com.bavykin.qualistrata.unix.environment.detector;

import com.bavykin.qualistrata.core.environment.EnvironmentCheck;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckResult;
import com.bavykin.qualistrata.core.environment.EnvironmentCheckSeverity;
import com.bavykin.qualistrata.unix.facade.UnixFacade;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Environment check that validates a Unix component through a registered
 * generic detector.
 */
public class DetectedUnixComponentEnvironmentCheck implements EnvironmentCheck {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final String id;
    private final String name;
    private final EnvironmentCheckSeverity severity;
    private final UnixFacade unixFacade;
    private final String hostAlias;
    private final String userAlias;
    private final UnixComponentDetectionRequest request;
    private final UnixComponentDetectorRegistry detectorRegistry;
    private final Duration timeout;

    /**
     * Creates a detected Unix component check using the default detector
     * registry and default timeout.
     *
     * @param id stable check id.
     * @param name human-readable check name.
     * @param severity severity used when the check fails.
     * @param unixFacade Unix facade.
     * @param hostAlias host alias.
     * @param request detection request.
     */
    public DetectedUnixComponentEnvironmentCheck(
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            UnixFacade unixFacade,
            String hostAlias,
            UnixComponentDetectionRequest request
    ) {
        this(
                id,
                name,
                severity,
                unixFacade,
                hostAlias,
                null,
                request,
                new DefaultUnixComponentDetectorRegistry(),
                DEFAULT_TIMEOUT
        );
    }

    /**
     * Creates a detected Unix component check.
     *
     * @param id stable check id.
     * @param name human-readable check name.
     * @param severity severity used when the check fails.
     * @param unixFacade Unix facade.
     * @param hostAlias host alias.
     * @param userAlias optional user alias.
     * @param request detection request.
     * @param detectorRegistry detector registry.
     * @param timeout detector command timeout.
     */
    public DetectedUnixComponentEnvironmentCheck(
            String id,
            String name,
            EnvironmentCheckSeverity severity,
            UnixFacade unixFacade,
            String hostAlias,
            String userAlias,
            UnixComponentDetectionRequest request,
            UnixComponentDetectorRegistry detectorRegistry,
            Duration timeout
    ) {
        if (id == null || id.isBlank()) {
            throw new UnixComponentDetectorException("Environment check id must not be blank.");
        }

        if (name == null || name.isBlank()) {
            throw new UnixComponentDetectorException("Environment check name must not be blank.");
        }

        if (severity == null) {
            throw new UnixComponentDetectorException("Environment check severity must not be null.");
        }

        if (unixFacade == null) {
            throw new UnixComponentDetectorException("Unix facade must not be null.");
        }

        if (hostAlias == null || hostAlias.isBlank()) {
            throw new UnixComponentDetectorException("Unix host alias must not be blank.");
        }

        if (request == null) {
            throw new UnixComponentDetectorException("Unix component detection request must not be null.");
        }

        if (detectorRegistry == null) {
            throw new UnixComponentDetectorException("Unix component detector registry must not be null.");
        }

        this.id = id.trim();
        this.name = name.trim();
        this.severity = severity;
        this.unixFacade = unixFacade;
        this.hostAlias = hostAlias.trim();
        this.userAlias = normalizeOptional(userAlias);
        this.request = request;
        this.detectorRegistry = detectorRegistry;
        this.timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
    }

    /**
     * Executes detector-based Unix component validation.
     *
     * @return environment check result.
     */
    @Override
    public EnvironmentCheckResult check() {
        long startNanos = System.nanoTime();

        try {
            UnixComponentDetector detector = detectorRegistry.detector(request.detector());

            UnixComponentDetectionResult detectionResult = detector.detect(
                    unixFacade,
                    hostAlias,
                    userAlias,
                    request,
                    timeout
            );

            Map<String, String> details = details(detectionResult);

            if (detectionResult.matched()) {
                return EnvironmentCheckResult.passed(
                        id,
                        name,
                        detectionResult.message(),
                        elapsedSince(startNanos),
                        details
                );
            }

            return EnvironmentCheckResult.failed(
                    id,
                    name,
                    severity,
                    detectionResult.message(),
                    elapsedSince(startNanos),
                    details,
                    null
            );
        } catch (RuntimeException exception) {
            Map<String, String> details = baseDetails();

            details.put("exceptionType", exception.getClass().getName());

            return EnvironmentCheckResult.failed(
                    id,
                    name,
                    severity,
                    "Unix component detector check failed with exception: " + exception.getMessage(),
                    elapsedSince(startNanos),
                    details,
                    exception
            );
        }
    }

    /**
     * Builds diagnostic details from a detection result.
     *
     * @param result detection result.
     * @return diagnostic details.
     */
    private Map<String, String> details(UnixComponentDetectionResult result) {
        Map<String, String> details = baseDetails();

        details.putAll(result.details());

        return details;
    }

    /**
     * Builds common diagnostic details.
     *
     * @return common details.
     */
    private Map<String, String> baseDetails() {
        Map<String, String> details = new LinkedHashMap<>();

        details.put("hostAlias", hostAlias);
        details.put("userAlias", userAlias == null ? "" : userAlias);
        details.put("shellProfile", request.shellProfile() == null ? "" : request.shellProfile());

        return details;
    }

    /**
     * Normalizes optional string value.
     *
     * @param value raw value.
     * @return trimmed value, or {@code null}.
     */
    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    /**
     * Calculates elapsed duration since the given monotonic timestamp.
     *
     * @param startNanos start timestamp from {@link System#nanoTime()}.
     * @return elapsed duration.
     */
    private Duration elapsedSince(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }
}