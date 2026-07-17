package com.bavykin.qualistrata.report.model;

import java.util.List;

/**
 * Policy used to calculate the final status of a logical test from its attempts.
 */
public enum FinalStatusPolicy {

    /**
     * Uses the status of the last observed attempt.
     *
     * <p>This is the canonical retry policy: a failed attempt followed by a
     * successful retry produces a final {@link TestStatus#PASSED} status while
     * preserving the earlier failure in the attempt history.</p>
     */
    LAST_ATTEMPT {
        @Override
        public TestStatus resolve(List<TestAttempt> attempts) {
            requireAttempts(attempts);
            return attempts.getLast().result().status();
        }
    };

    /**
     * Resolves the final logical-test status.
     *
     * @param attempts non-empty ordered attempt history
     * @return final logical-test status
     */
    public abstract TestStatus resolve(List<TestAttempt> attempts);

    private static void requireAttempts(List<TestAttempt> attempts) {
        if (attempts == null || attempts.isEmpty()) {
            throw new IllegalArgumentException("At least one attempt is required");
        }
    }
}
