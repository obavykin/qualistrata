package com.bavykin.qualistrata.ui.junit;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JUnit 5 marker for UI tests that must not run together with other live tests.
 *
 * <p>Examples include tests that reset global application state, consume a
 * shared batch slot, or otherwise need exclusive access to a target
 * environment.</p>
 *
 * <p>{@link QualiStrataJunitUiExtension} enforces this marker with a
 * target-scoped environment write lock. Every other QualiStrata JUnit UI test
 * acquires the corresponding environment read lock, so an exclusive test waits
 * for active UI tests on the same target and prevents new ones from starting
 * until it completes. JUnit {@code @Isolated} is therefore not required for
 * this purpose. Lock coordination is process-local and does not span separate
 * test JVMs or Gradle forks.</p>
 */
@Tag(UiTestTags.UI)
@Tag(UiTestTags.EXCLUSIVE)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresExclusiveEnvironment {
}
