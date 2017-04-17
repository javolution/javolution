/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a parameter, variable or method return value can be {@code null}. 
 * Unless specified by this annotation, {@code null} values are prohibited for parameters, 
 * variables and methods return values. 
 */
@Documented
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Nullable  {

    /**
     * Indicates if this element supports {@code null} value (default {@code true}).
     * 
     * @return true if {@code null} value is supported
     */
    boolean value() default true;

    /**
     * Provides additional information (default {@code ""}).
     * 
     * @return comment
     */
    String comment() default "";

}
