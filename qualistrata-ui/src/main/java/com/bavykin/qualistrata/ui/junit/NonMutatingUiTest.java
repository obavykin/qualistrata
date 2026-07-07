package com.bavykin.qualistrata.ui.junit;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JUnit 5 marker for read-only UI tests that can normally run in parallel.
 *
 * <p>The annotation also adds the standard {@code ui} and {@code non-mutating}
 * JUnit tags so Gradle/JUnit suites can select these tests without depending on
 * reflection over QualiStrata-specific annotations.</p>
 */
@Tag(UiTestTags.UI)
@Tag(UiTestTags.NON_MUTATING)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface NonMutatingUiTest {
}
