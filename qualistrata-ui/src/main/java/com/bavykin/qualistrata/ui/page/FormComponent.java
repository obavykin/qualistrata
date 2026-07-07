package com.bavykin.qualistrata.ui.page;

import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.locator.UiLocator;

/**
 * Generic form component helper.
 */
public class FormComponent extends BaseComponent {

    /** Creates form component. */
    public FormComponent(UiSession session, UiLocator root) {
        super(session, root);
    }

    /**
     * Fills input by visible label.
     *
     * @param label field label.
     * @param value value to enter.
     */
    public void fillByLabel(String label, String value) {
        session.element(UiLocator.inputByLabel(label)).fill(value);
    }

    /**
     * Clicks a button by visible text.
     *
     * @param text button text.
     */
    public void clickButton(String text) {
        session.element(UiLocator.button(text)).click();
    }
}
