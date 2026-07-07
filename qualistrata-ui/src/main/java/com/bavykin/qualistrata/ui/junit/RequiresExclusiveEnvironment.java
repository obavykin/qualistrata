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
 */
@Tag(UiTestTags.UI)
@Tag(UiTestTags.EXCLUSIVE)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresExclusiveEnvironment {
}
