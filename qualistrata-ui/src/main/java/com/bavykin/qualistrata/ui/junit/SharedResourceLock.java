package com.bavykin.qualistrata.ui.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares shared external state that must be coordinated between parallel
 * JUnit UI tests.
 *
 * <p>Tests holding {@link SharedResourceAccessMode#READ READ} access to the same
 * resource may execute concurrently. {@link SharedResourceAccessMode#WRITE WRITE}
 * access waits for existing readers and writers and prevents new access until
 * the owning test has completed its full before/after lifecycle.</p>
 *
 * <p>The annotation is repeatable and may be placed on a test class or method.
 * Class and method declarations are combined. When the same effective resource
 * is declared more than once, WRITE takes precedence over READ.</p>
 *
 * <p>Coordination is process-local. Separate JVMs or Gradle forks do not share
 * the in-memory lock registry.</p>
 */
@Inherited
@Repeatable(SharedResourceLocks.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SharedResourceLock {

    /**
     * Stable consumer-defined resource name, for example
     * {@code ADMIN_OPTIONS:PIVOT_SZ}.
     *
     * @return non-blank resource name.
     */
    String value();

    /**
     * Requested access mode.
     *
     * @return READ by default.
     */
    SharedResourceAccessMode mode() default SharedResourceAccessMode.READ;

    /**
     * Scope used to build the effective resource identity.
     *
     * @return TARGET by default.
     */
    SharedResourceScope scope() default SharedResourceScope.TARGET;
}
