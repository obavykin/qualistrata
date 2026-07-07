package com.bavykin.qualistrata.ui.wait;

/**
 * Boolean condition used by generic wait helpers.
 */
@FunctionalInterface
public interface WaitCondition {

    /** @return whether condition is satisfied. */
    boolean isSatisfied();
}
