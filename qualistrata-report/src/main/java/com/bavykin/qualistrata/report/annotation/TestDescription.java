package com.bavykin.qualistrata.report.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a human-readable description to a test class or test method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TestDescription {

    /**
     * Returns the human-readable test description.
     *
     * @return non-blank description
     */
    String value();
}
