package com.bavykin.qualistrata.ui.junit;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JUnit 5 marker for UI tests that change product or environment state.
 *
 * <p>Mutating tests are not considered safe for default method-level parallel
 * execution unless the consumer project provides its own data isolation model.</p>
 */
@Tag(UiTestTags.UI)
@Tag(UiTestTags.MUTATING)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MutatingUiTest {
}
