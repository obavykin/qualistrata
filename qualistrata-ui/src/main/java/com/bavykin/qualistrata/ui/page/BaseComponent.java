package com.bavykin.qualistrata.ui.page;

import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.element.UiElement;
import com.bavykin.qualistrata.ui.locator.UiLocator;

/**
 * Generic component object rooted at a locator.
 *
 * <p>The root locator represents a reusable part of a page such as menu, form,
 * table, modal body, toolbar, or card. Product-specific component behaviour is
 * implemented in consumer modules.</p>
 */
public abstract class BaseComponent {

    /** Active UI session. */
    protected final UiSession session;
    /** Root locator of this component. */
    protected final UiLocator root;

    /**
     * Creates component object.
     *
     * @param session active UI session.
     * @param root root locator of the component.
     */
    protected BaseComponent(UiSession session, UiLocator root) {
        if (session == null) {
            throw new IllegalArgumentException("UI session must not be null.");
        }

        if (root == null) {
            throw new IllegalArgumentException("Component root locator must not be null.");
        }

        this.session = session;
        this.root = root;
    }

    /**
     * Returns the root element facade.
     *
     * @return root element facade.
     */
    public UiElement root() {
        return session.element(root);
    }

    /** Waits until component root is visible. */
    public void waitUntilVisible() {
        root().waitUntilVisible();
    }
}
