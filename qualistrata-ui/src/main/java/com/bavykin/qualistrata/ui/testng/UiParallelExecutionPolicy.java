package com.bavykin.qualistrata.ui.testng;

/**
 * Decides whether a classified UI test may run in parallel.
 */
public final class UiParallelExecutionPolicy {

    /**
     * Creates a UI parallel-execution policy.
     */
    public UiParallelExecutionPolicy() {
    }

    /**
     * Checks whether the classification may run in parallel.
     *
     * @param mutationType mutation classification to evaluate.
     * @return whether tests with the given classification may run in parallel.
     */
    public boolean mayRunInParallel(UiMutationType mutationType) {
        return mutationType == UiMutationType.NON_MUTATING;
    }

    /**
     * Checks whether the classification requires sequential execution.
     *
     * @param mutationType mutation classification to evaluate.
     * @return whether tests with the given classification require sequential execution.
     */
    public boolean requiresSequentialRun(UiMutationType mutationType) {
        return mutationType == UiMutationType.MUTATING || mutationType == UiMutationType.EXCLUSIVE;
    }

    /**
     * Returns the conventional TestNG group for the classification.
     *
     * @param mutationType mutation classification to map.
     * @return conventional TestNG group for the mutation type.
     */
    public String groupFor(UiMutationType mutationType) {
        if (mutationType == null) {
            return null;
        }

        return switch (mutationType) {
            case NON_MUTATING -> UiTestGroups.NON_MUTATING;
            case MUTATING -> UiTestGroups.MUTATING;
            case EXCLUSIVE -> UiTestGroups.EXCLUSIVE;
        };
    }
}
