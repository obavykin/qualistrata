package com.bavykin.qualistrata.ui.testng;

/**
 * Decides whether a classified UI test may run in parallel.
 */
public final class UiParallelExecutionPolicy {

    /** @return whether tests with given classification may run in parallel. */
    public boolean mayRunInParallel(UiMutationType mutationType) {
        return mutationType == UiMutationType.NON_MUTATING;
    }

    /** @return whether tests with given classification require sequential run. */
    public boolean requiresSequentialRun(UiMutationType mutationType) {
        return mutationType == UiMutationType.MUTATING || mutationType == UiMutationType.EXCLUSIVE;
    }
}
