/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> Indicates that the state of a class instance, a method return value, 
 *     a static field or a method parameter should not change after creation 
 *     or method invocation (for method return value or method parameter).
 * [code]
 * @Constant
 * public class ConstantTable<F> extends FastTable<F> {
 *     public static <E> ConstantTable<E> of(@Constant E... elements ) { ... }
 *     public @Constant unmodifiable() { return this; } 
 *     ...
 * }
 * 
 * [/code]
 *   
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.1, February 2, 2014
 */
@Documented
@Inherited
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
	       ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
public @interface Constant  {

    /**
     * Indicates if this element is constant (default {@code true}).
     */
    boolean value() default true;

    /**
     * Provides additional information (default {@code ""}).
     */
    String comment() default "";

}
