package com.bavykin.qualistrata.core.config.selection;

/**
 * Result of active target selection.
 *
 * <p>The record stores both the selected target name and the source from which
 * it was selected. Keeping the source improves diagnostics and helps framework
 * reports explain why a particular environment was used.</p>
 *
 * @param name selected target name.
 * @param source source from which the target name was selected.
 */
public record SelectedTarget(
        String name,
        TargetSelectionSource source
) {
}