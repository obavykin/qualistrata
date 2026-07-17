package com.bavykin.qualistrata.report.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation generated for repeated {@link CoversTestCase} entries.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CoversTestCases {

    /**
     * Returns the repeated test-case references.
     *
     * @return coverage references
     */
    CoversTestCase[] value();
}
