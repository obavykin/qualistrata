package com.bavykin.qualistrata.ui.testng;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a UI test that requires exclusive environment access.
 *
 * <p>Equivalent TestNG group: {@link UiTestGroups#EXCLUSIVE}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresExclusiveEnvironment {
}
