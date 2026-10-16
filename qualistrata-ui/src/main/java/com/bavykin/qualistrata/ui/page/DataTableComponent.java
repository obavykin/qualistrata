package com.bavykin.qualistrata.ui.page;

import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.element.UiElementCollection;
import com.bavykin.qualistrata.ui.locator.UiLocator;

import java.util.List;

/**
 * Generic HTML-like data table helper.
 *
 * <p>The component exposes only generic table capabilities such as headers and
 * row count. Report-specific filters, export actions, and business validation
 * must be implemented in consumer modules.</p>
 */
public class DataTableComponent extends BaseComponent {

    /**
     * Creates a data table component.
     *
     * @param session active UI session.
     * @param root root table locator.
     */
    public DataTableComponent(UiSession session, UiLocator root) {
        super(session, root);
    }

    /**
     * Returns visible header texts found via the generic {@code th} selector.
     *
     * @return visible header texts.
     */
    public List<String> headers() {
        return session.elements(UiLocator.css("th")).texts();
    }

    /**
     * Returns the visible row count found via the generic {@code tbody tr} selector.
     *
     * @return visible row count.
     */
    public int rowCount() {
        return session.elements(UiLocator.css("tbody tr")).count();
    }

    /**
     * Verifies that table has expected column names.
     *
     * @param expectedColumns expected columns.
     */
    public void shouldHaveColumns(String... expectedColumns) {
        List<String> actualHeaders = headers();
        for (String expectedColumn : expectedColumns) {
            if (!actualHeaders.contains(expectedColumn)) {
                throw new AssertionError("Expected table column '" + expectedColumn + "' was not found in " + actualHeaders + ".");
            }
        }
    }

    /**
     * Returns the generic table body rows.
     *
     * @return collection of generic body rows.
     */
    public UiElementCollection rows() {
        return session.elements(UiLocator.css("tbody tr"));
    }
}
