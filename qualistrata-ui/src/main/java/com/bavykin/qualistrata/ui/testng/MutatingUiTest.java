package com.bavykin.qualistrata.ui.testng;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a UI test that changes product data and should not be parallelized by default.
 *
 * <p>Equivalent TestNG group: {@link UiTestGroups#MUTATING}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MutatingUiTest {
}
