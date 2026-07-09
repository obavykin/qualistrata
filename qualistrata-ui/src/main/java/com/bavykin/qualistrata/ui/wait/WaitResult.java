package com.bavykin.qualistrata.ui.wait;

import java.time.Duration;

/**
 * Result of a condition-based wait.
 *
 * @param successful whether condition became true.
 * @param elapsed elapsed wait time.
 * @param attempts number of condition attempts.
 */
public record WaitResult(boolean successful, Duration elapsed, int attempts) {
}
