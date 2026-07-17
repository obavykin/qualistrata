package com.bavykin.qualistrata.report.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Links an automated test to one external or manual test-case reference.
 */
@Documented
@Repeatable(CoversTestCases.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CoversTestCase {

    /**
     * Returns the external test-case identifier.
     *
     * @return non-blank test-case identifier
     */
    String value();

    /**
     * Returns the optional source system, such as qTest, Jira, or TestRail.
     *
     * @return source-system name or an empty string when unspecified
     */
    String system() default "";
}
