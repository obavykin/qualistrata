package com.bavykin.qualistrata.ui.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation supporting repeated {@link SharedResourceLock}
 * declarations.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SharedResourceLocks {

    /**
     * Declared shared-resource locks.
     *
     * @return lock declarations.
     */
    SharedResourceLock[] value();
}
