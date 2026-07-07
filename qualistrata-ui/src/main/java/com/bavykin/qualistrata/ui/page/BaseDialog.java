package com.bavykin.qualistrata.ui.page;

import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.locator.UiLocator;

/**
 * Generic modal/dialog component base.
 *
 * <p>Dialogs are common UI primitives and are safe to keep in the generic UI
 * module. Product-specific dialog titles, buttons, and workflows belong to
 * consumer modules.</p>
 */
public abstract class BaseDialog extends BaseComponent {

    protected BaseDialog(UiSession session, UiLocator root) {
        super(session, root);
    }

    /** @return whether the dialog root is currently visible. */
    public boolean isOpen() {
        return root().isVisible();
    }
}
