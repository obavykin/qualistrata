package com.bavykin.qualistrata.report.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Associates a test class or method with one product feature.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Feature {

    /**
     * Returns the feature name.
     *
     * @return non-blank feature name
     */
    String value();
}
