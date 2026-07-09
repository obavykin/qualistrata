package com.bavykin.qualistrata.core.config.selection;

import com.bavykin.qualistrata.core.exceptions.FrameworkException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link TargetSelector}.
 *
 * <p>The tests document the generic target selection priority used by
 * QualiStrata core. The selector must preserve the useful behavior from the old
 * project implementation while removing product-specific defaults such as
 * release names from the reusable framework.</p>
 */
class TargetSelectorTest {

    private final TargetSelector selector = new TargetSelector();

    @Test
    void shouldSelectTargetFromSystemPropertyFirst() {
        Properties properties = new Properties();
        properties.setProperty(TargetSelector.TARGET_SYSTEM_PROPERTY, "from-property");

        Map<String, String> environment = Map.of(
                TargetSelector.TARGET_ENVIRONMENT_VARIABLE,
                "from-environment"
        );

        SelectedTarget selected = selector.select(properties, environment, "from-default");

        assertEquals("from-property", selected.name());
        assertEquals(TargetSelectionSource.SYSTEM_PROPERTY, selected.source());
    }

    @Test
    void shouldSelectTargetFromEnvironmentWhenSystemPropertyIsMissing() {
        Properties properties = new Properties();

        Map<String, String> environment = Map.of(
                TargetSelector.TARGET_ENVIRONMENT_VARIABLE,
                "from-environment"
        );

        SelectedTarget selected = selector.select(properties, environment, "from-default");

        assertEquals("from-environment", selected.name());
        assertEquals(TargetSelectionSource.ENVIRONMENT_VARIABLE, selected.source());
    }

    @Test
    void shouldSelectExplicitProjectDefaultWhenRuntimeValuesAreMissing() {
        SelectedTarget selected = selector.select(
                new Properties(),
                Map.of(),
                "from-default"
        );

        assertEquals("from-default", selected.name());
        assertEquals(TargetSelectionSource.PROJECT_DEFAULT, selected.source());
    }

    @Test
    void shouldTrimSelectedTargetName() {
        Properties properties = new Properties();
        properties.setProperty(TargetSelector.TARGET_SYSTEM_PROPERTY, "  demo  ");

        SelectedTarget selected = selector.select(properties, Map.of());

        assertEquals("demo", selected.name());
        assertEquals(TargetSelectionSource.SYSTEM_PROPERTY, selected.source());
    }

    @Test
    void shouldIgnoreBlankSystemPropertyAndUseEnvironmentVariable() {
        Properties properties = new Properties();
        properties.setProperty(TargetSelector.TARGET_SYSTEM_PROPERTY, "   ");

        Map<String, String> environment = Map.of(
                TargetSelector.TARGET_ENVIRONMENT_VARIABLE,
                "from-environment"
        );

        SelectedTarget selected = selector.select(properties, environment);

        assertEquals("from-environment", selected.name());
        assertEquals(TargetSelectionSource.ENVIRONMENT_VARIABLE, selected.source());
    }

    @Test
    void shouldIgnoreBlankEnvironmentVariableAndUseProjectDefault() {
        Map<String, String> environment = Map.of(
                TargetSelector.TARGET_ENVIRONMENT_VARIABLE,
                "   "
        );

        SelectedTarget selected = selector.select(
                new Properties(),
                environment,
                "from-default"
        );

        assertEquals("from-default", selected.name());
        assertEquals(TargetSelectionSource.PROJECT_DEFAULT, selected.source());
    }

    @Test
    void shouldFailWhenNoTargetCanBeSelected() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> selector.select(new Properties(), Map.of())
        );

        assertEquals(
                "Target was not specified. Provide -Dtarget, TARGET environment variable, "
                        + "or an explicit project-level default.",
                exception.getMessage()
        );
    }

    @Test
    void shouldHandleNullRuntimeSources() {
        SelectedTarget selected = selector.select(null, null, "from-default");

        assertEquals("from-default", selected.name());
        assertEquals(TargetSelectionSource.PROJECT_DEFAULT, selected.source());
    }

    @Test
    void shouldFailWhenRuntimeSourcesAndDefaultAreNull() {
        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> selector.select(null, null, null)
        );

        assertEquals(
                "Target was not specified. Provide -Dtarget, TARGET environment variable, "
                        + "or an explicit project-level default.",
                exception.getMessage()
        );
    }
}