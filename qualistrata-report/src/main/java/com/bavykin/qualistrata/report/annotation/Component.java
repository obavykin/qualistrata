package com.bavykin.qualistrata.report.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Associates a test class or method with one application or system component.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Component {

    /**
     * Returns the component name.
     *
     * @return non-blank component name
     */
    String value();
}
