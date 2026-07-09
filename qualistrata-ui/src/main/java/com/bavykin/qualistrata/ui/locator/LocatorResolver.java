package com.bavykin.qualistrata.ui.locator;

/**
 * Adapter-specific locator resolver.
 *
 * @param <N> native locator type used by the selected browser engine.
 */
public interface LocatorResolver<N> {

    /**
     * Resolves generic locator into native engine representation.
     *
     * @param locator generic locator.
     * @return native locator.
     */
    N resolve(UiLocator locator);
}
